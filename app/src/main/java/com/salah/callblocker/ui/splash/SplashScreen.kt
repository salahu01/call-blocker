package com.salah.callblocker.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salah.callblocker.ui.icons.AppIcons
import com.salah.callblocker.ui.theme.lime_neon
import com.salah.callblocker.ui.theme.md_dark_background
import com.salah.callblocker.ui.theme.md_dark_onBackground
import com.salah.callblocker.ui.theme.md_dark_onSurfaceVariant
import com.salah.callblocker.ui.theme.on_lime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

/**
 * Branded launch splash: a glowing lime shield with an expanding radar ring,
 * the app name and tagline easing in, then a clean fade-out to the dashboard.
 *
 * Kept on a fixed dark brand backdrop (independent of system light/dark) so it
 * lines up seamlessly with the cold-start system splash defined in themes.xml.
 */
@Composable
fun SplashScreen(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val logoScale = remember { Animatable(0.72f) }
    val logoAlpha = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val textShift = remember { Animatable(18f) }
    val contentAlpha = remember { Animatable(1f) }

    val pulse = rememberInfiniteTransition(label = "splash-pulse")
    val glow by pulse.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow",
    )
    val ring by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring",
    )

    LaunchedEffect(Unit) {
        launch { logoAlpha.animateTo(1f, tween(420)) }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = 0.52f,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
        delay(220)
        launch { textShift.animateTo(0f, tween(520, easing = FastOutSlowInEasing)) }
        textAlpha.animateTo(1f, tween(620))
        delay(900)
        contentAlpha.animateTo(0f, tween(360))
        onFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF141A09), md_dark_background),
                    radius = 1100f,
                ),
            )
            .alpha(contentAlpha.value),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ── Glowing shield logo with radar ring ──
            Box(
                modifier = Modifier.size(196.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.fillMaxSize().alpha(logoAlpha.value)) {
                    val c = Offset(size.width / 2f, size.height / 2f)
                    val maxR = min(size.width, size.height) / 2f

                    // Soft pulsing glow halo behind the badge.
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                lime_neon.copy(alpha = glow * 0.42f),
                                Color.Transparent,
                            ),
                            center = c,
                            radius = maxR,
                        ),
                        radius = maxR,
                        center = c,
                    )

                    // Expanding radar ring that fades as it grows.
                    val ringR = maxR * (0.42f + ring * 0.55f)
                    drawCircle(
                        color = lime_neon.copy(alpha = (1f - ring) * 0.55f),
                        radius = ringR,
                        center = c,
                        style = Stroke(width = 2.dp.toPx()),
                    )
                }

                // Lime badge + dark shield glyph.
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                        .clip(CircleShape)
                        .background(lime_neon),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = AppIcons.shield,
                        contentDescription = null,
                        tint = on_lime,
                        modifier = Modifier.size(58.dp),
                    )
                }
            }

            // ── Wordmark + tagline ──
            Spacer(Modifier.size(28.dp))
            Text(
                text = "Globber",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 46.sp,
                    letterSpacing = (-1.5).sp,
                ),
                color = md_dark_onBackground,
                modifier = Modifier
                    .offset(y = textShift.value.dp)
                    .alpha(textAlpha.value),
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text = "Block spam by pattern, not just by number",
                style = MaterialTheme.typography.bodyMedium,
                color = md_dark_onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(y = textShift.value.dp)
                    .alpha(textAlpha.value * 0.9f)
                    .padding(horizontal = 48.dp),
            )
        }
    }
}
