package com.salah.callblocker.ui

import android.Manifest
import android.app.role.RoleManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salah.callblocker.ui.components.BentoCard
import com.salah.callblocker.ui.components.BentoVariant
import com.salah.callblocker.ui.components.CircleIconButton
import com.salah.callblocker.ui.components.GlassBackdrop
import com.salah.callblocker.ui.components.PillButton
import com.salah.callblocker.ui.icons.AppIcons
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import com.salah.callblocker.ui.dashboard.DashboardScreen
import com.salah.callblocker.ui.log.LogScreen
import com.salah.callblocker.ui.rules.RulesScreen
import com.salah.callblocker.ui.settings.SettingsScreen
import com.salah.callblocker.ui.settings.SettingsViewModel
import com.salah.callblocker.ui.splash.SplashScreen
import com.salah.callblocker.ui.theme.CallBlockerTheme
import com.salah.callblocker.ui.theme.LocalCallBlockerColors
import kotlinx.coroutines.launch

private const val DASHBOARD = "dashboard"
private const val RULES = "rules"
private const val LOG = "log"
private const val SETTINGS = "settings"

private val TOP_BAR_HEIGHT = 64.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CallBlockerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    var showSplash by remember { mutableStateOf(true) }
                    Crossfade(
                        targetState = showSplash,
                        animationSpec = tween(400),
                        label = "splash-to-app",
                    ) { splash ->
                        if (splash) {
                            SplashScreen(onFinished = { showSplash = false })
                        } else {
                            CallBlockerApp()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallBlockerApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    val settingsVm: SettingsViewModel = viewModel()
    val hazeState = remember { HazeState() }

    var screen by remember { mutableStateOf(DASHBOARD) }

    fun isRoleHeld(): Boolean {
        val roleManager = context.getSystemService(RoleManager::class.java)
        return roleManager?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
    }

    var roleHeld by remember { mutableStateOf(isRoleHeld()) }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        roleHeld = isRoleHeld()
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val json = settingsVm.exportJson()
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val json = context.contentResolver.openInputStream(uri)
                ?.bufferedReader()
                ?.use { it.readText() }
            if (json != null) settingsVm.importJson(json, replace = false)
        }
    }

    val contactsPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        settingsVm.setAllowContacts(granted)
    }

    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                roleHeld = isRoleHeld()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(enabled = screen != DASHBOARD) { screen = DASHBOARD }

    Box(modifier = Modifier.fillMaxSize()) {
        GlassBackdrop(modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            when (screen) {
                RULES -> RulesScreen(
                    onBack = { screen = DASHBOARD },
                    modifier = Modifier.fillMaxSize(),
                )
                LOG -> LogScreen(
                    onBack = { screen = DASHBOARD },
                    modifier = Modifier.fillMaxSize(),
                )
                SETTINGS -> SettingsScreen(
                    vm = settingsVm,
                    onExport = { exportLauncher.launch("callblocker-rules.json") },
                    onImport = { importLauncher.launch(arrayOf("application/json", "text/*")) },
                    onEnableContactsAllowlist = {
                        contactsPermLauncher.launch(Manifest.permission.READ_CONTACTS)
                    },
                    onBack = { screen = DASHBOARD },
                    modifier = Modifier.fillMaxSize(),
                )
                else -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        DashboardScreen(
                            onOpenLog = { screen = LOG },
                            onOpenRules = { screen = RULES },
                            contentTopPadding = TOP_BAR_HEIGHT + 8.dp,
                            topBanner = if (!roleHeld) {
                                {
                                    RoleBanner(
                                        onRequestRole = {
                                            val roleManager =
                                                context.getSystemService(RoleManager::class.java)
                                            if (roleManager != null) {
                                                val intent = roleManager.createRequestRoleIntent(
                                                    RoleManager.ROLE_CALL_SCREENING,
                                                )
                                                roleLauncher.launch(intent)
                                            }
                                        },
                                    )
                                }
                            } else {
                                null
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .hazeSource(hazeState),
                        )
                        DashboardTopBar(
                            onOpenRules = { screen = RULES },
                            onOpenSettings = { screen = SETTINGS },
                            hazeState = hazeState,
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardTopBar(
    onOpenRules: () -> Unit,
    onOpenSettings: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(TOP_BAR_HEIGHT)
            .hazeEffect(state = hazeState) {
                blurRadius = 24.dp
                backgroundColor = scheme.background
                tints = listOf(HazeTint(scheme.background.copy(alpha = 0.55f)))
            }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIconButton(
            icon = AppIcons.shield,
            contentDescription = "Rules",
            onClick = onOpenRules,
            outlined = true,
        )
        CircleIconButton(
            icon = AppIcons.settings,
            contentDescription = "Settings",
            onClick = onOpenSettings,
            outlined = true,
        )
    }
}

@Composable
private fun RoleBanner(
    onRequestRole: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accents = LocalCallBlockerColors.current
    BentoCard(
        modifier = modifier.fillMaxWidth(),
        variant = BentoVariant.Accent,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Not set as call screening app",
                style = MaterialTheme.typography.titleMedium,
                color = accents.onAccent,
            )
            Text(
                text = "Call Blocker must be set as your phone's call screening app to " +
                    "block incoming calls. Without this permission no calls can be blocked.",
                style = MaterialTheme.typography.bodyMedium,
                color = accents.onAccent.copy(alpha = 0.85f),
            )
            PillButton(
                text = "Set as call screening app",
                onClick = onRequestRole,
                filled = false,
            )
        }
    }
}
