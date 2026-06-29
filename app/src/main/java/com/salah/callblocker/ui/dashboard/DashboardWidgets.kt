package com.salah.callblocker.ui.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
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
 * "Most Blocked" viz — the repeat offender. A central "no-calls" ban badge
 * (phone glyph crossed by a slash) with concentric ripple rings pulsing
 * outward: the number keeps calling, keeps getting blocked. The badge gives a
 * small recoil pulse each time a fresh ripple is born.
 * [base] = muted ring color, [highlight] = badge / lead-ripple color.
 */
@Composable
fun MostBlockedViz(base: Color, highlight: Color, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "mostBlocked")
    val wave by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2400, easing = LinearEasing), RepeatMode.Restart),
        label = "wave",
    )
    val beat by t.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing),
            RepeatMode.Reverse,
        ),
        label = "beat",
    )
    Canvas(modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val center = Offset(cx, cy)
        val badgeR = size.minDimension * 0.20f
        val maxR = size.minDimension * 0.52f
        val stroke = 2.dp.toPx()

        // Expanding call-signal ripples, three phased rings fading as they grow.
        for (k in 0 until 3) {
            val p = (wave + k / 3f) % 1f
            val r = badgeR + (maxR - badgeR) * p
            drawCircle(
                color = highlight.copy(alpha = (1f - p) * 0.5f),
                radius = r,
                center = center,
                style = Stroke(width = stroke),
            )
        }

        // Ban badge: ring + phone glyph + diagonal slash, gently beating.
        val r = badgeR * beat
        drawCircle(highlight, r, center, style = Stroke(width = 2.4.dp.toPx()))
        // simple phone handset glyph inside the badge
        val pr = r * 0.46f
        drawLine(
            color = highlight,
            start = Offset(cx - pr, cy - pr),
            end = Offset(cx + pr * 0.2f, cy + pr * 0.6f),
            strokeWidth = 2.6.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = highlight,
            start = Offset(cx + pr * 0.2f, cy + pr * 0.6f),
            end = Offset(cx + pr, cy - pr * 0.2f),
            strokeWidth = 2.6.dp.toPx(),
            cap = StrokeCap.Round,
        )
        // slash across the badge — the "blocked" mark
        val d = r * 0.7071f
        drawLine(
            color = highlight,
            start = Offset(cx - d, cy - d),
            end = Offset(cx + d, cy + d),
            strokeWidth = 2.4.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

/**
 * "Active Rules" viz — a filter funnel. Call dots fall in from the top, the
 * funnel screens them, and a lime catch-dot pulses at the throat as each one
 * passes the active rule. [base] = funnel outline color, [highlight] = the
 * active-rule accent (falling dots + catch pulse).
 */
@Composable
fun ActiveRulesViz(base: Color, highlight: Color, modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "activeRules")
    val drop by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
        label = "drop",
    )
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.dp.toPx()
        val outline = base.copy(alpha = 0.45f)

        // Funnel geometry.
        val topY = h * 0.14f
        val throatY = h * 0.60f
        val outLeftX = w * 0.16f
        val outRightX = w * 0.84f
        val throatLX = w * 0.43f
        val throatRX = w * 0.57f
        val stemBottomY = h * 0.84f

        // Funnel walls + stem.
        drawLine(outline, Offset(outLeftX, topY), Offset(throatLX, throatY), stroke, cap = StrokeCap.Round)
        drawLine(outline, Offset(outRightX, topY), Offset(throatRX, throatY), stroke, cap = StrokeCap.Round)
        drawLine(outline, Offset(throatLX, throatY), Offset(throatLX, stemBottomY), stroke, cap = StrokeCap.Round)
        drawLine(outline, Offset(throatRX, throatY), Offset(throatRX, stemBottomY), stroke, cap = StrokeCap.Round)

        // Two phased call-dots falling into the funnel mouth.
        val dotR = w * 0.05f
        for (k in 0 until 2) {
            val p = (drop + k * 0.5f) % 1f
            // funnel converges, so the dot's x drifts toward center as it falls
            val y = topY + (throatY - topY) * p
            val converge = 0.5f - (0.5f - 0.34f) * p
            val x = w * (0.30f + (0.40f) * (k.toFloat())) // start spread
            val cxDot = x + (w * converge - x) * p
            drawCircle(
                color = highlight.copy(alpha = 0.35f + 0.65f * p),
                radius = dotR,
                center = Offset(cxDot, y),
            )
        }

        // Lime catch pulse at the throat — fires as a dot completes its fall.
        val phase = drop % 0.5f / 0.5f
        val pulse = (1f - phase)
        drawCircle(
            color = highlight,
            radius = dotR * (0.8f + 0.9f * (1f - pulse)),
            center = Offset(w * 0.5f, throatY + h * 0.06f),
            alpha = pulse,
        )
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
