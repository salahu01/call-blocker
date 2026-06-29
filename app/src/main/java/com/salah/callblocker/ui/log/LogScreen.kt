package com.salah.callblocker.ui.log

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salah.callblocker.data.BlockedCall
import com.salah.callblocker.data.PatternType
import com.salah.callblocker.data.RuleAction
import com.salah.callblocker.ui.components.BentoCard
import com.salah.callblocker.ui.components.BentoVariant
import com.salah.callblocker.ui.components.CircleIconButton
import com.salah.callblocker.ui.components.ScreenHeader
import com.salah.callblocker.ui.icons.AppIcons
import com.salah.callblocker.ui.icons.icon
import com.salah.callblocker.ui.theme.LocalCallBlockerColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private fun PatternType.displayName(): String = when (this) {
    PatternType.EXACT -> "Exact"
    PatternType.STARTS_WITH -> "Starts with"
    PatternType.CONTAINS -> "Contains"
    PatternType.ENDS_WITH -> "Ends with"
    PatternType.REGEX -> "Regex"
}

private fun RuleAction.displayName(): String = when (this) {
    RuleAction.REJECT -> "Reject"
    RuleAction.SILENCE -> "Silence"
    RuleAction.VOICEMAIL -> "Voicemail"
}

private enum class ActionFilter(val label: String, val match: RuleAction?) {
    ALL("All", null),
    REJECT("Reject", RuleAction.REJECT),
    SILENCE("Silence", RuleAction.SILENCE),
    VOICEMAIL("Voicemail", RuleAction.VOICEMAIL),
}

private data class DaySection(val label: String, val calls: List<BlockedCall>)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogScreen(
    modifier: Modifier = Modifier,
    viewModel: LogViewModel = viewModel(),
    onBack: () -> Unit = {},
) {
    val calls by viewModel.calls.collectAsStateWithLifecycle()
    val timeFmt = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    var filter by remember { mutableStateOf(ActionFilter.ALL) }
    var newestFirst by remember { mutableStateOf(true) }

    val sections = remember(calls, filter, newestFirst) {
        groupByDay(
            calls = calls.filter { filter.match == null || it.action == filter.match },
            newestFirst = newestFirst,
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Blocked Calls",
            onBack = onBack,
            action = {
                CircleIconButton(
                    icon = AppIcons.trash,
                    contentDescription = "Clear log",
                    onClick = { viewModel.clear() },
                    enabled = calls.isNotEmpty(),
                    outlined = true,
                    tint = MaterialTheme.colorScheme.error,
                )
            },
        )

        if (calls.isEmpty()) {
            EmptyLog(Modifier.fillMaxSize())
        } else {
            FilterSortBar(
                filter = filter,
                onFilter = { filter = it },
                newestFirst = newestFirst,
                onToggleSort = { newestFirst = !newestFirst },
            )

            if (sections.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No ${filter.label.lowercase()} calls.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 16.dp,
                    ),
                ) {
                    sections.forEach { section ->
                        stickyHeader(key = "h:${section.label}") {
                            DayHeader(section.label, section.calls.size)
                        }
                        items(section.calls, key = { it.id }) { call ->
                            LogRow(call = call, time = timeFmt.format(Date(call.timestamp)))
                            Spacer(Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterSortBar(
    filter: ActionFilter,
    onFilter: (ActionFilter) -> Unit,
    newestFirst: Boolean,
    onToggleSort: () -> Unit,
) {
    val accents = LocalCallBlockerColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ActionFilter.entries.forEach { f ->
                FilterChipPill(
                    text = f.label,
                    selected = f == filter,
                    onClick = { onFilter(f) },
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable(onClick = onToggleSort)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (newestFirst) AppIcons.chevronDown else AppIcons.chevronUp,
                contentDescription = "Toggle sort",
                tint = accents.accentFill,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = if (newestFirst) "Newest" else "Oldest",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Composable
private fun FilterChipPill(text: String, selected: Boolean, onClick: () -> Unit) {
    val accents = LocalCallBlockerColors.current
    val bg = if (selected) accents.accentFill else MaterialTheme.colorScheme.surfaceContainerHighest
    val fg = if (selected) accents.onAccent else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = fg)
    }
}

@Composable
private fun DayHeader(label: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (count == 1) "1 call" else "$count calls",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyLog(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        BentoCard(variant = BentoVariant.Dark) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                EmptyLogArt(
                    modifier = Modifier
                        .padding(top = 4.dp, bottom = 20.dp)
                        .size(168.dp),
                )
                Text(
                    text = "No blocked calls yet.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Blocked calls will show up here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

/** Abstract empty-state mark: faint signal rings + phone handset behind a block ring + slash. */
@Composable
private fun EmptyLogArt(modifier: Modifier = Modifier) {
    val accent = LocalCallBlockerColors.current.accentFill
    Canvas(modifier = modifier) {
        val w = size.width
        val cx = w / 2f
        val cy = size.height / 2f
        val s = w / 1024f
        fun px(x: Float) = x * s

        // faint signal rings
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

        // phone handset glyph (normalized 1024 geometry), centered + scaled inside the ring
        val phone = Path().apply {
            moveTo(px(360f), px(360f))
            cubicTo(px(360f), px(330f), px(390f), px(322f), px(412f), px(338f))
            lineTo(px(470f), px(382f))
            cubicTo(px(492f), px(398f), px(496f), px(420f), px(484f), px(444f))
            lineTo(px(470f), px(470f))
            cubicTo(px(506f), px(540f), px(560f), px(594f), px(630f), px(630f))
            lineTo(px(656f), px(616f))
            cubicTo(px(680f), px(604f), px(702f), px(608f), px(718f), px(630f))
            lineTo(px(762f), px(688f))
            cubicTo(px(778f), px(710f), px(770f), px(740f), px(740f), px(740f))
            cubicTo(px(560f), px(740f), px(360f), px(540f), px(360f), px(360f))
            close()
        }
        val phoneCenter = Offset(px(561f), px(539f))
        translate(left = cx - phoneCenter.x, top = cy - phoneCenter.y) {
            scale(scale = 0.62f, pivot = phoneCenter) {
                drawPath(
                    path = phone,
                    color = accent.copy(alpha = 0.85f),
                    style = Stroke(width = w * 0.045f),
                )
            }
        }

        // block ring + diagonal slash = blocked
        val ringR = w * 0.26f
        val ringC = Offset(cx, cy)
        drawCircle(
            color = accent,
            radius = ringR,
            center = ringC,
            style = Stroke(width = w * 0.045f),
        )
        val d = (ringR * 0.70710677f) // r / sqrt(2)
        drawLine(
            color = accent,
            start = Offset(ringC.x - d, ringC.y - d),
            end = Offset(ringC.x + d, ringC.y + d),
            strokeWidth = w * 0.045f,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun LogRow(call: BlockedCall, time: String) {
    val accents = LocalCallBlockerColors.current
    BentoCard(variant = BentoVariant.Dark, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = call.number,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    Icon(
                        imageVector = call.matchedType.icon(),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .size(15.dp),
                    )
                    Text(
                        text = "\"${call.matchedPattern}\" · ${call.matchedType.displayName()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                ActionTag(call.action)
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun ActionTag(action: RuleAction) {
    val accents = LocalCallBlockerColors.current
    val color: Color = when (action) {
        RuleAction.REJECT -> MaterialTheme.colorScheme.error
        RuleAction.SILENCE -> MaterialTheme.colorScheme.onSurfaceVariant
        RuleAction.VOICEMAIL -> accents.accentFill
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(
            text = action.displayName(),
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

/** Buckets calls into day sections (Today / Yesterday / date), each internally sorted. */
private fun groupByDay(calls: List<BlockedCall>, newestFirst: Boolean): List<DaySection> {
    if (calls.isEmpty()) return emptyList()

    val dayFmt = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())
    val dayFmtYear = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    fun dayStart(ts: Long): Long = Calendar.getInstance().apply {
        timeInMillis = ts
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val today = dayStart(System.currentTimeMillis())
    val oneDay = 24L * 60 * 60 * 1000
    val curYear = Calendar.getInstance().get(Calendar.YEAR)

    fun label(dayKey: Long): String = when (dayKey) {
        today -> "Today"
        today - oneDay -> "Yesterday"
        else -> {
            val cal = Calendar.getInstance().apply { timeInMillis = dayKey }
            if (cal.get(Calendar.YEAR) == curYear) dayFmt.format(Date(dayKey))
            else dayFmtYear.format(Date(dayKey))
        }
    }

    val sorted = calls.sortedBy { it.timestamp }.let { if (newestFirst) it.reversed() else it }
    // group preserving order of first appearance
    val buckets = LinkedHashMap<Long, MutableList<BlockedCall>>()
    sorted.forEach { call ->
        buckets.getOrPut(dayStart(call.timestamp)) { mutableListOf() }.add(call)
    }
    return buckets.map { (dayKey, list) -> DaySection(label(dayKey), list) }
}
