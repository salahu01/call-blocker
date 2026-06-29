package com.salah.callblocker.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.unit.dp
import com.salah.callblocker.data.PatternType
import kotlin.math.cos
import kotlin.math.sin

/** Glyph for each match-pattern type. */
fun PatternType.icon(): ImageVector = when (this) {
    PatternType.EXACT -> AppIcons.patternExact
    PatternType.STARTS_WITH -> AppIcons.patternStartsWith
    PatternType.CONTAINS -> AppIcons.patternContains
    PatternType.ENDS_WITH -> AppIcons.patternEndsWith
    PatternType.REGEX -> AppIcons.patternRegex
}

/**
 * Custom hand-built thin-line icon set (24x24 viewport, stroke-based).
 *
 * Stroke color is a placeholder (Black) — Material3 `Icon(tint = …)` applies a
 * `ColorFilter.tint` over the whole painter, so every glyph inherits its call-site tint.
 */
object AppIcons {

    private fun lineIcon(name: String, build: PathBuilder.() -> Unit): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = PathData(build),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }.build()

    /** Adds a full circle subpath via two half-arcs. */
    private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
        moveTo(cx + r, cy)
        arcTo(r, r, 0f, true, false, cx - r, cy)
        arcTo(r, r, 0f, true, false, cx + r, cy)
    }

    /** Adds a rounded-rectangle subpath. */
    private fun PathBuilder.roundRect(l: Float, t: Float, r: Float, b: Float, rad: Float) {
        moveTo(l + rad, t)
        lineTo(r - rad, t); quadTo(r, t, r, t + rad)
        lineTo(r, b - rad); quadTo(r, b, r - rad, b)
        lineTo(l + rad, b); quadTo(l, b, l, b - rad)
        lineTo(l, t + rad); quadTo(l, t, l + rad, t)
        close()
    }

    /** Four evenly-spaced vertical "digit" bars. */
    private fun PathBuilder.digitBars(y0: Float, y1: Float) {
        floatArrayOf(7f, 10.33f, 13.67f, 17f).forEach { x ->
            moveTo(x, y0); lineTo(x, y1)
        }
    }

    val arrowRight: ImageVector by lazy {
        lineIcon("AppArrowRight") {
            moveTo(4f, 12f); lineTo(20f, 12f)
            moveTo(13f, 5f); lineTo(20f, 12f); lineTo(13f, 19f)
        }
    }

    val search: ImageVector by lazy {
        lineIcon("AppSearch") {
            circle(11f, 11f, 6f)
            moveTo(15.5f, 15.5f); lineTo(20f, 20f)
        }
    }

    val sort: ImageVector by lazy {
        lineIcon("AppSort") {
            moveTo(5f, 7.5f); lineTo(16f, 7.5f)
            moveTo(5f, 12f); lineTo(12.5f, 12f)
            moveTo(5f, 16.5f); lineTo(9f, 16.5f)
            // descending arrow on the right
            moveTo(17.5f, 9f); lineTo(17.5f, 17f)
            moveTo(15f, 14.5f); lineTo(17.5f, 17f); lineTo(20f, 14.5f)
        }
    }

    val arrowLeft: ImageVector by lazy {
        lineIcon("AppArrowLeft") {
            moveTo(20f, 12f); lineTo(4f, 12f)
            moveTo(11f, 5f); lineTo(4f, 12f); lineTo(11f, 19f)
        }
    }

    val chevronUp: ImageVector by lazy {
        lineIcon("AppChevronUp") {
            moveTo(5f, 15f); lineTo(12f, 8f); lineTo(19f, 15f)
        }
    }

    val chevronDown: ImageVector by lazy {
        lineIcon("AppChevronDown") {
            moveTo(5f, 9f); lineTo(12f, 16f); lineTo(19f, 9f)
        }
    }

    val plus: ImageVector by lazy {
        lineIcon("AppPlus") {
            moveTo(12f, 4f); lineTo(12f, 20f)
            moveTo(4f, 12f); lineTo(20f, 12f)
        }
    }

    val trash: ImageVector by lazy {
        lineIcon("AppTrash") {
            // lid
            moveTo(4f, 6.5f); lineTo(20f, 6.5f)
            // handle
            moveTo(9f, 6.5f); lineTo(9.5f, 4.5f); lineTo(14.5f, 4.5f); lineTo(15f, 6.5f)
            // body
            moveTo(6f, 6.5f); lineTo(7f, 20f); lineTo(17f, 20f); lineTo(18f, 6.5f)
            // inner streaks
            moveTo(10f, 10f); lineTo(10.4f, 16.5f)
            moveTo(14f, 10f); lineTo(13.6f, 16.5f)
        }
    }

    val shield: ImageVector by lazy {
        lineIcon("AppShield") {
            moveTo(12f, 3f)
            lineTo(19f, 6f)
            lineTo(19f, 11.5f)
            curveTo(19f, 16f, 16f, 19f, 12f, 20.5f)
            curveTo(8f, 19f, 5f, 16f, 5f, 11.5f)
            lineTo(5f, 6f)
            close()
            // check mark
            moveTo(9f, 11.6f); lineTo(11.2f, 13.8f); lineTo(15f, 9.6f)
        }
    }

    val settings: ImageVector by lazy {
        lineIcon("AppSettings") {
            val bodyR = 5.5f
            val toothInner = 5.5f
            val toothOuter = 8.5f
            // gear body ring + center hole
            circle(12f, 12f, bodyR)
            circle(12f, 12f, 2f)
            // 8 radial teeth
            for (i in 0 until 8) {
                val a = Math.toRadians((i * 45).toDouble())
                val cosA = cos(a).toFloat()
                val sinA = sin(a).toFloat()
                moveTo(12f + cosA * toothInner, 12f + sinA * toothInner)
                lineTo(12f + cosA * toothOuter, 12f + sinA * toothOuter)
            }
        }
    }

    // ── Pattern-type glyphs ─────────────────────────────────────────────
    // Cohesive family: the number is a row of four "digit" bars; a rounded
    // "match window" frames the matched segment — whole (exact), left
    // (starts), middle (contains) or right (ends). Regex breaks the grammar
    // with parentheses around a wildcard star.

    /** EXACT — match window wraps the whole number. */
    val patternExact: ImageVector by lazy {
        lineIcon("AppPatternExact") {
            digitBars(9.5f, 14.5f)
            roundRect(4.3f, 6.3f, 19.7f, 17.7f, 2.6f)
        }
    }

    /** STARTS_WITH — match window anchored over the leading digits. */
    val patternStartsWith: ImageVector by lazy {
        lineIcon("AppPatternStartsWith") {
            digitBars(9.5f, 14.5f)
            roundRect(4.3f, 6.3f, 12.2f, 17.7f, 2.6f)
        }
    }

    /** CONTAINS — match window floats over the middle digits. */
    val patternContains: ImageVector by lazy {
        lineIcon("AppPatternContains") {
            digitBars(9.5f, 14.5f)
            roundRect(8.4f, 6.3f, 15.6f, 17.7f, 2.6f)
        }
    }

    /** ENDS_WITH — match window anchored over the trailing digits. */
    val patternEndsWith: ImageVector by lazy {
        lineIcon("AppPatternEndsWith") {
            digitBars(9.5f, 14.5f)
            roundRect(11.8f, 6.3f, 19.7f, 17.7f, 2.6f)
        }
    }

    /** REGEX — parentheses framing a wildcard star. */
    val patternRegex: ImageVector by lazy {
        lineIcon("AppPatternRegex") {
            // left paren
            moveTo(8.6f, 6.5f); quadTo(5.6f, 12f, 8.6f, 17.5f)
            // right paren
            moveTo(15.4f, 6.5f); quadTo(18.4f, 12f, 15.4f, 17.5f)
            // wildcard star
            moveTo(12f, 9.2f); lineTo(12f, 14.8f)
            moveTo(9.6f, 10.6f); lineTo(14.4f, 13.4f)
            moveTo(14.4f, 10.6f); lineTo(9.6f, 13.4f)
        }
    }
}
