package com.salah.callblocker.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.salah.callblocker.data.ThemeMode
import com.salah.callblocker.ui.components.BentoCard
import com.salah.callblocker.ui.components.BentoVariant
import com.salah.callblocker.ui.components.PillButton
import com.salah.callblocker.ui.components.ScreenHeader
import com.salah.callblocker.ui.components.SectionHeader
import com.salah.callblocker.ui.theme.LocalCallBlockerColors

@Composable
fun SettingsScreen(
    vm: SettingsViewModel,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onEnableContactsAllowlist: () -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val allowContacts by vm.allowContacts.collectAsStateWithLifecycle()
    val notifyOnBlock by vm.notifyOnBlock.collectAsStateWithLifecycle()
    val blockUnknown by vm.blockUnknown.collectAsStateWithLifecycle()
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenHeader(title = "Settings", onBack = onBack)

        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        SectionHeader(title = "Calls")
        BentoCard(variant = BentoVariant.Dark, modifier = Modifier.fillMaxWidth()) {
            SettingSwitchRow(
                title = "Allow calls from contacts",
                subtitle = "Calls from people saved in your contacts are never blocked.",
                checked = allowContacts,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        onEnableContactsAllowlist()
                        vm.setAllowContacts(true)
                    } else {
                        vm.setAllowContacts(false)
                    }
                },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            SettingSwitchRow(
                title = "Block unknown numbers",
                subtitle = "Reject calls with no caller ID (private or withheld numbers).",
                checked = blockUnknown,
                onCheckedChange = { vm.setBlockUnknown(it) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            SettingSwitchRow(
                title = "Notify when a call is blocked",
                subtitle = "Show a notification each time a call is blocked.",
                checked = notifyOnBlock,
                onCheckedChange = { vm.setNotifyOnBlock(it) },
            )
        }

        SectionHeader(title = "Appearance")
        BentoCard(variant = BentoVariant.Dark, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Choose how the app looks. System follows your device setting.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
            )
            ThemeModeSelector(
                selected = themeMode,
                onSelect = { vm.setThemeMode(it) },
            )
        }

        SectionHeader(title = "Backup")
        BentoCard(variant = BentoVariant.Dark, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Save all your rules to a JSON file.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            PillButton(text = "Export rules", onClick = onExport, modifier = Modifier.fillMaxWidth())
            Text(
                text = "Add rules from a previously exported JSON file.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            )
            PillButton(
                text = "Import rules",
                onClick = onImport,
                filled = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        }
    }
}

@Composable
private fun ThemeModeSelector(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
) {
    val accents = LocalCallBlockerColors.current
    val options = listOf(
        ThemeMode.SYSTEM to "System",
        ThemeMode.LIGHT to "Light",
        ThemeMode.DARK to "Dark",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (mode, label) ->
            val isSelected = mode == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSelected) accents.accentFill else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onSelect(mode) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) accents.onAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val accents = LocalCallBlockerColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = accents.accentFill,
                checkedThumbColor = accents.onAccent,
            ),
        )
    }
}
