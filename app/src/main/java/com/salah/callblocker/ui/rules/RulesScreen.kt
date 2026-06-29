package com.salah.callblocker.ui.rules

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salah.callblocker.data.BlockRule
import com.salah.callblocker.data.PatternType
import com.salah.callblocker.data.RuleAction
import com.salah.callblocker.ui.components.BentoCard
import com.salah.callblocker.ui.components.BentoVariant
import com.salah.callblocker.ui.components.CircleIconButton
import com.salah.callblocker.ui.components.PillButton
import com.salah.callblocker.ui.components.ScreenHeader
import com.salah.callblocker.ui.icons.AppIcons
import com.salah.callblocker.ui.icons.icon
import com.salah.callblocker.ui.theme.LocalCallBlockerColors

fun PatternType.displayName(): String = when (this) {
    PatternType.EXACT -> "Exact"
    PatternType.STARTS_WITH -> "Starts with"
    PatternType.CONTAINS -> "Contains"
    PatternType.ENDS_WITH -> "Ends with"
    PatternType.REGEX -> "Regex"
}

fun RuleAction.displayName(): String = when (this) {
    RuleAction.REJECT -> "Reject"
    RuleAction.SILENCE -> "Silence"
    RuleAction.VOICEMAIL -> "Voicemail"
}

@Composable
fun RulesScreen(
    modifier: Modifier = Modifier,
    viewModel: RulesViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
    val rules by viewModel.rules.collectAsStateWithLifecycle()
    val accents = LocalCallBlockerColors.current
    // null = no dialog; non-null wrapper holds the rule being edited (or null inside = add)
    var editorState by remember { mutableStateOf<EditorState?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = { ScreenHeader(title = "Rules", onBack = onBack) },
        floatingActionButton = {
            if (rules.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { editorState = EditorState(null) },
                    containerColor = accents.accentFill,
                    contentColor = accents.onAccent,
                    shape = RoundedCornerShape(50),
                    icon = { Icon(AppIcons.plus, contentDescription = null) },
                    text = { Text("Add rule") },
                )
            }
        },
    ) { innerPadding ->
        if (rules.isEmpty()) {
            EmptyRules(
                onAdd = { editorState = EditorState(null) },
                modifier = Modifier.padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = innerPadding.calculateBottomPadding() + 88.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(rules, key = { it.id }) { rule ->
                    RuleRow(
                        rule = rule,
                        onToggle = { viewModel.toggle(rule) },
                        onDelete = { viewModel.delete(rule) },
                        onMoveUp = { viewModel.moveUp(rule) },
                        onMoveDown = { viewModel.moveDown(rule) },
                        onClick = { editorState = EditorState(rule) },
                    )
                }
            }
        }
    }

    editorState?.let { state ->
        RuleEditorDialog(
            initial = state.rule,
            onDismiss = { editorState = null },
            onConfirm = { pattern, type, action, label ->
                val initial = state.rule
                if (initial == null) {
                    viewModel.addRule(pattern, type, label, action)
                } else {
                    viewModel.updateRule(
                        initial.copy(
                            pattern = pattern,
                            type = type,
                            label = label,
                            action = action,
                        )
                    )
                }
                editorState = null
            },
        )
    }
}

private class EditorState(val rule: BlockRule?)

@Composable
private fun EmptyRules(onAdd: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        BentoCard(variant = BentoVariant.Dark) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EmptyRulesArt(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 20.dp)
                        .size(168.dp),
                )
                Text(
                    text = "No blocking rules yet.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Add a number or pattern to start blocking calls.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )
                PillButton(text = "Add your first rule", onClick = onAdd)
            }
        }
    }
}

/** Abstract empty-state mark: ghost pattern rings + shield + wildcard asterisk. */
@Composable
private fun EmptyRulesArt(modifier: Modifier = Modifier) {
    val accent = LocalCallBlockerColors.current.accentFill
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // faint "pattern net" rings
        val dash = PathEffect.dashPathEffect(floatArrayOf(w * 0.04f, w * 0.05f))
        drawCircle(
            color = accent.copy(alpha = 0.16f),
            radius = w * 0.46f,
            center = Offset(cx, cy),
            style = Stroke(width = w * 0.012f, pathEffect = dash),
        )
        drawCircle(
            color = accent.copy(alpha = 0.10f),
            radius = w * 0.30f,
            center = Offset(cx, cy),
            style = Stroke(width = w * 0.012f, pathEffect = dash),
        )

        // ghost shield outline (normalized 1024 geometry scaled to canvas)
        val s = w / 1024f
        fun px(x: Float) = x * s
        fun py(y: Float) = y * s
        val shield = Path().apply {
            moveTo(px(322f), py(248f))
            lineTo(px(702f), py(248f))
            quadraticBezierTo(px(744f), py(248f), px(744f), py(290f))
            lineTo(px(744f), py(474f))
            cubicTo(px(744f), py(624f), px(662f), py(724f), px(512f), py(802f))
            cubicTo(px(362f), py(724f), px(280f), py(624f), px(280f), py(474f))
            lineTo(px(280f), py(290f))
            quadraticBezierTo(px(280f), py(248f), px(322f), py(248f))
            close()
        }
        drawPath(
            path = shield,
            color = accent.copy(alpha = 0.55f),
            style = Stroke(width = w * 0.035f),
        )

        // wildcard asterisk in the shield center
        val ast = Offset(px(512f), py(512f))
        val armLen = w * 0.11f
        val armW = w * 0.045f
        for (i in 0 until 3) {
            val ang = Math.toRadians((i * 60).toDouble())
            val dx = (Math.cos(ang) * armLen).toFloat()
            val dy = (Math.sin(ang) * armLen).toFloat()
            drawLine(
                color = accent,
                start = Offset(ast.x - dx, ast.y - dy),
                end = Offset(ast.x + dx, ast.y + dy),
                strokeWidth = armW,
                cap = StrokeCap.Round,
            )
        }
        drawCircle(color = accent, radius = w * 0.028f, center = ast)
    }
}

@Composable
private fun TagPill(text: String, leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
    val accents = LocalCallBlockerColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .border(1.dp, accents.accentFill, RoundedCornerShape(50))
            .background(accents.accentFill.copy(alpha = 0.10f))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = accents.accentFill,
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(16.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun RuleRow(
    rule: BlockRule,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onClick: () -> Unit,
) {
    val accents = LocalCallBlockerColors.current
    BentoCard(
        variant = BentoVariant.Dark,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = 16,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.label.ifBlank { rule.pattern },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = rule.pattern,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                TagPill(
                    text = "${rule.type.displayName()} · ${rule.action.displayName()}",
                    leadingIcon = rule.type.icon(),
                )
            }
            Switch(
                checked = rule.enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = accents.accentFill,
                    checkedThumbColor = accents.onAccent,
                ),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CircleIconButton(
                icon = AppIcons.chevronUp,
                contentDescription = "Move up",
                onClick = onMoveUp,
                outlined = true,
            )
            CircleIconButton(
                icon = AppIcons.chevronDown,
                contentDescription = "Move down",
                onClick = onMoveDown,
                outlined = true,
            )
            Box(modifier = Modifier.weight(1f))
            CircleIconButton(
                icon = AppIcons.trash,
                contentDescription = "Delete rule",
                onClick = onDelete,
                outlined = true,
                tint = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RuleEditorDialog(
    initial: BlockRule?,
    onDismiss: () -> Unit,
    onConfirm: (pattern: String, type: PatternType, action: RuleAction, label: String) -> Unit,
) {
    var pattern by remember { mutableStateOf(initial?.pattern ?: "") }
    var label by remember { mutableStateOf(initial?.label ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: PatternType.EXACT) }
    var action by remember { mutableStateOf(initial?.action ?: RuleAction.REJECT) }
    var typeExpanded by remember { mutableStateOf(false) }
    var actionExpanded by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .imePadding()
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = if (initial == null) "Add rule" else "Edit rule",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text("Pattern") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = it },
                ) {
                    OutlinedTextField(
                        value = type.displayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Match type") },
                        leadingIcon = {
                            Icon(
                                imageVector = type.icon(),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                    ) {
                        PatternType.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName()) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = option.icon(),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                },
                                onClick = {
                                    type = option
                                    typeExpanded = false
                                },
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = actionExpanded,
                    onExpandedChange = { actionExpanded = it },
                ) {
                    OutlinedTextField(
                        value = action.displayName(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Action") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    DropdownMenu(
                        expanded = actionExpanded,
                        onDismissRequest = { actionExpanded = false },
                    ) {
                        RuleAction.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.displayName()) },
                                onClick = {
                                    action = option
                                    actionExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            ) {
                PillButton(text = "Cancel", onClick = onDismiss, filled = false)
                PillButton(
                    text = if (initial == null) "Add" else "Save",
                    onClick = { onConfirm(pattern.trim(), type, action, label.trim()) },
                    enabled = pattern.isNotBlank(),
                )
            }
        }
    }
}
