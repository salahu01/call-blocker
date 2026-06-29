package com.salah.callblocker.ui.dashboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.salah.callblocker.ui.components.MiniTrack
import com.salah.callblocker.ui.components.glassSurface
import com.salah.callblocker.ui.icons.AppIcons
import com.salah.callblocker.ui.theme.LocalCallBlockerColors

/**
 * Reference hero slider: dark rounded track, a solid green thumb at [progress],
 * a white capsule cap on the far left and two white capsule segment ticks near the right.
 */
@Composable
fun HeroSlider(progress: Float, modifier: Modifier = Modifier) {
    val accents = LocalCallBlockerColors.current
    val track = accents.track
    val thumb = accents.accentFill
    val cap = MaterialTheme.colorScheme.onSurface
    Canvas(modifier.height(24.dp)) {
        val cy = size.height / 2f
        val trackH = 6.dp.toPx()
        val r = trackH / 2f
        drawRoundRect(
            color = track,
            topLeft = Offset(0f, cy - r),
            size = Size(size.width, trackH),
            cornerRadius = CornerRadius(r, r),
        )
        val capW = 13.dp.toPx()
        val capH = 7.dp.toPx()
        val capR = CornerRadius(capH / 2f, capH / 2f)
        // far-left white capsule cap
        drawRoundRect(
            color = cap,
            topLeft = Offset(0f, cy - capH / 2f),
            size = Size(capW, capH),
            cornerRadius = capR,
        )
        // two white capsule segment ticks near the right edge
        val gap = 6.dp.toPx()
        val t2x = size.width - capW
        val t1x = t2x - capW - gap
        listOf(t1x, t2x).forEach { x ->
            drawRoundRect(
                color = cap,
                topLeft = Offset(x, cy - capH / 2f),
                size = Size(capW, capH),
                cornerRadius = capR,
            )
        }
        // solid green thumb
        val tx = size.width * progress.coerceIn(0.1f, 0.62f)
        drawCircle(thumb, 9.dp.toPx(), Offset(tx, cy))
    }
}

/** One of the three hero sub-metrics: value + label + mini colored track. */
@Composable
fun SubMetric(
    value: String,
    label: String,
    color: Color,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        MiniTrack(
            progress = progress,
            modifier = Modifier.fillMaxWidth(),
            showThumb = false,
            fillColor = color,
        )
    }
}

/**
 * "Most Blocked" abstract viz — a frequency histogram. Bars rise to a peak
 * (the repeat offender) which is highlighted and capped with a small block ring;
 * the rest are muted. [base] = muted bar color, [highlight] = peak color.
 */
@Composable
fun MostBlockedViz(base: Color, highlight: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val heights = floatArrayOf(0.40f, 0.62f, 0.82f, 1.0f, 0.55f, 0.34f, 0.48f)
        val peak = 3
        val n = heights.size
        val gap = size.width * 0.05f
        val barW = (size.width - gap * (n - 1)) / n
        val maxH = size.height * 0.86f
        val baseY = size.height
        val radius = CornerRadius(barW / 2f, barW / 2f)
        heights.forEachIndexed { i, frac ->
            val h = maxH * frac
            val x = i * (barW + gap)
            drawRoundRect(
                color = if (i == peak) highlight else base.copy(alpha = 0.35f),
                topLeft = Offset(x, baseY - h),
                size = Size(barW, h),
                cornerRadius = radius,
            )
        }
        // block ring crowning the peak bar
        val px = peak * (barW + gap) + barW / 2f
        val py = baseY - maxH - 2.dp.toPx()
        val rr = barW * 0.55f
        drawCircle(highlight, rr, Offset(px, py), style = Stroke(width = 2.dp.toPx()))
        val d = rr * 0.7071f
        drawLine(
            highlight,
            Offset(px - d, py - d),
            Offset(px + d, py + d),
            strokeWidth = 2.dp.toPx(),
        )
    }
}

/**
 * "Active Rules" abstract viz — a stack of rule rows. Each row is a rounded
 * capsule with a leading status dot; the top (active) row is highlighted, the
 * rest muted. [base] = muted row color, [highlight] = active row color.
 */
@Composable
fun ActiveRulesViz(base: Color, highlight: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val widths = floatArrayOf(1.0f, 0.78f, 0.6f)
        val activeIdx = 0
        val n = widths.size
        val rowH = size.height * 0.20f
        val gap = (size.height - rowH * n) / (n - 1) * 0.7f
        val dotR = rowH * 0.42f
        val dotCx = dotR
        widths.forEachIndexed { i, frac ->
            val active = i == activeIdx
            val color = if (active) highlight else base.copy(alpha = 0.30f)
            val y = i * (rowH + gap)
            val cy = y + rowH / 2f
            // status dot
            if (active) {
                drawCircle(color, dotR, Offset(dotCx, cy))
            } else {
                drawCircle(color, dotR, Offset(dotCx, cy), style = Stroke(width = 1.5.dp.toPx()))
            }
            // rule capsule
            val barX = dotR * 2f + size.width * 0.05f
            val barMax = size.width - barX
            drawRoundRect(
                color = if (active) color else base.copy(alpha = 0.22f),
                topLeft = Offset(barX, y),
                size = Size(barMax * frac, rowH),
                cornerRadius = CornerRadius(rowH / 2f, rowH / 2f),
            )
        }
    }
}

/** Bottom "Total" pill row with a trailing outlined arrow (reference Total Workload row). */
@Composable
fun TotalRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val border = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f)
    Row(
        modifier = modifier
            .glassSurface(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(BorderStroke(1.dp, border), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                AppIcons.arrowRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
