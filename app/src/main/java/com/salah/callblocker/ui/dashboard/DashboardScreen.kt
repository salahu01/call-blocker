package com.salah.callblocker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.salah.callblocker.data.BlockedCall
import com.salah.callblocker.ui.components.BentoCard
import com.salah.callblocker.ui.components.BentoVariant
import com.salah.callblocker.ui.components.CircleIconButton
import com.salah.callblocker.ui.components.DeltaText
import com.salah.callblocker.ui.components.PillButton
import com.salah.callblocker.ui.components.SectionHeader
import com.salah.callblocker.ui.icons.AppIcons
import com.salah.callblocker.ui.theme.LocalCallBlockerColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    viewModel: DashboardViewModel = viewModel(),
    onOpenLog: () -> Unit = {},
    onOpenRules: () -> Unit = {},
    contentTopPadding: Dp = 8.dp,
    topBanner: (@Composable () -> Unit)? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val timeFmt = remember { SimpleDateFormat("MMM d · h:mm a", Locale.getDefault()) }

    Column(modifier = modifier.fillMaxWidth()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = contentTopPadding,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (topBanner != null) {
                item { topBanner() }
            }

            item { HeroCard(state, onOpenLog) }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                MostBlockedCard(state, Modifier.weight(1f))
                RulesCard(state, onOpenRules, Modifier.weight(1f))
            }
        }

        item {
            TotalRow(
                label = "Total Blocked",
                value = if (state.totalBlocked == 1) "1 call" else "${state.totalBlocked} calls",
                onClick = onOpenLog,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        item {
            SectionHeader(
                title = "Recent activity",
                action = {
                    if (state.recent.isNotEmpty()) {
                        PillButton(text = "See all", onClick = onOpenLog, filled = false)
                    }
                },
            )
        }

        if (state.recent.isEmpty()) {
            item {
                BentoCard(variant = BentoVariant.Dark, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No blocked calls yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            items(state.recent, key = { it.id }) { call ->
                RecentRow(call = call, formatted = timeFmt.format(Date(call.timestamp)))
            }
        }
        }
    }
}

@Composable
private fun HeroCard(state: DashboardUiState, onOpenLog: () -> Unit) {
    val accents = LocalCallBlockerColors.current
    val weekTrack = state.blockedThisWeek.toFloat() / max(1, state.totalBlocked)
    val rulesTrack = state.activeRules.toFloat() / max(1, state.totalRules)
    BentoCard(variant = BentoVariant.Dark, modifier = Modifier.fillMaxWidth(), contentPadding = 22) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text(
                    text = "Calls Blocked",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "All time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            CircleIconButton(
                icon = AppIcons.arrowRight,
                contentDescription = "Log",
                onClick = onOpenLog,
                outlined = true,
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = state.totalBlocked.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Light,
            )
            Text(
                text = "TOTAL",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp, bottom = 12.dp),
            )
            Spacer(Modifier.weight(1f))
            state.weekDelta?.let {
                DeltaText(it, unit = "wk", modifier = Modifier.padding(bottom = 12.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        HeroSlider(progress = state.todayTrack, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SubMetric(
                value = state.blockedToday.toString(),
                label = "Today",
                color = accents.deltaPositive,
                progress = state.todayTrack,
                modifier = Modifier.weight(1f),
            )
            MetricDivider()
            SubMetric(
                value = state.blockedThisWeek.toString(),
                label = "This Week",
                color = MaterialTheme.colorScheme.onSurface,
                progress = weekTrack,
                modifier = Modifier.weight(1f),
            )
            MetricDivider()
            SubMetric(
                value = state.activeRules.toString(),
                label = "Active",
                color = accents.accentFill,
                progress = rulesTrack,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MetricDivider() {
    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .width(1.dp)
            .height(34.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
    )
}

@Composable
private fun MostBlockedCard(state: DashboardUiState, modifier: Modifier = Modifier) {
    val accents = LocalCallBlockerColors.current
    BentoCard(variant = BentoVariant.Accent, modifier = modifier, contentPadding = 18) {
        Text(
            text = "Most Blocked",
            style = MaterialTheme.typography.titleMedium,
            color = accents.onAccent,
        )
        Text(
            text = state.topBlockedNumber?.let { shortNumber(it) } ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            color = accents.onAccent.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = state.topBlockedCount.toString(),
                style = MaterialTheme.typography.displayMedium,
                color = accents.onAccent,
            )
            Text(
                text = "x",
                style = MaterialTheme.typography.labelLarge,
                color = accents.onAccent.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        MostBlockedViz(
            base = accents.onAccent,
            highlight = accents.onAccent,
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp),
        )
    }
}

@Composable
private fun RulesCard(state: DashboardUiState, onOpenRules: () -> Unit, modifier: Modifier = Modifier) {
    val accents = LocalCallBlockerColors.current
    BentoCard(
        variant = BentoVariant.Dark,
        modifier = modifier,
        onClick = onOpenRules,
        contentPadding = 18,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Active Rules",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "of ${state.totalRules}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = AppIcons.arrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = state.activeRules.toString(),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(8.dp))
        ActiveRulesViz(
            base = MaterialTheme.colorScheme.onSurface,
            highlight = accents.accentFill,
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp),
        )
    }
}

@Composable
private fun RecentRow(call: BlockedCall, formatted: String) {
    BentoCard(variant = BentoVariant.Dark, modifier = Modifier.fillMaxWidth(), contentPadding = 16) {
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
                Text(
                    text = formatted,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun shortNumber(n: String): String =
    if (n.length > 11) "…" + n.takeLast(8) else n
