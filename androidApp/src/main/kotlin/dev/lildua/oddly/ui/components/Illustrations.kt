package dev.lildua.oddly.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.ui.theme.OddlyColors
import dev.lildua.oddly.ui.theme.OddlyGradients
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Programmatic artwork. Everything here is drawn with Compose primitives rather
 * than shipped as an asset, which keeps the APK small and lets the illustrations
 * pick up theme colours automatically.
 */

private data class Star(val x: Float, val y: Float, val radius: Float, val alpha: Float)

/**
 * Faint starfield backdrop. Positions are generated once from a fixed seed so
 * the sky doesn't reshuffle on every recomposition.
 */
@Composable
fun StarField(
    modifier: Modifier = Modifier,
    starCount: Int = 60,
    seed: Int = 7,
) {
    val stars = remember(starCount, seed) {
        val random = Random(seed)
        List(starCount) {
            Star(
                x = random.nextFloat(),
                y = random.nextFloat(),
                radius = random.nextFloat() * 1.4f + 0.4f,
                alpha = random.nextFloat() * 0.5f + 0.15f,
            )
        }
    }

    Canvas(modifier) {
        stars.forEach { star ->
            drawCircle(
                color = Color.White.copy(alpha = star.alpha),
                radius = star.radius,
                center = Offset(star.x * size.width, star.y * size.height),
            )
        }
    }
}

/** Soft radial bloom placed behind hero content. */
@Composable
fun GlowOrb(
    color: Color,
    modifier: Modifier = Modifier,
    alpha: Float = 0.3f,
) {
    Canvas(modifier) {
        drawCircle(brush = OddlyGradients.glow(color, alpha), radius = size.minDimension / 2f)
    }
}

/**
 * The app mascot. Drawn as a friendly floating astronaut — a helmet with a
 * gradient visor, a rounded suit, and a gentle bob animation.
 */
@Composable
fun Astronaut(
    modifier: Modifier = Modifier,
    size: Dp = 160.dp,
    animated: Boolean = true,
) {
    val transition = rememberInfiniteTransition(label = "astronaut")
    val bob by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bob",
    )
    val offsetFactor = if (animated) bob else 0f

    Canvas(modifier.size(size)) {
        val s = this.size.minDimension
        translate(top = offsetFactor * s * 0.02f) {
            drawAstronaut(s)
        }
    }
}

private fun DrawScope.drawAstronaut(s: Float) {
    fun p(x: Float, y: Float) = Offset(x * s, y * s)

    val suit = Color(0xFFE9E6F7)
    val suitShadow = Color(0xFFC3BDDE)
    val visorBrush = Brush.linearGradient(
        listOf(OddlyColors.Purple, OddlyColors.Pink, OddlyColors.Indigo),
        start = p(0.36f, 0.14f),
        end = p(0.64f, 0.38f),
    )

    // Backpack, sits behind the torso.
    drawRoundRect(
        color = suitShadow,
        topLeft = p(0.3f, 0.4f),
        size = Size(0.4f * s, 0.3f * s),
        cornerRadius = CornerRadius(0.1f * s),
    )

    // Limbs first so the torso overlaps them cleanly.
    val limbWidth = 0.11f * s
    drawLine(suit, p(0.36f, 0.5f), p(0.18f, 0.62f), limbWidth, StrokeCap.Round)
    drawLine(suit, p(0.64f, 0.5f), p(0.83f, 0.58f), limbWidth, StrokeCap.Round)
    drawLine(suit, p(0.43f, 0.7f), p(0.38f, 0.88f), limbWidth, StrokeCap.Round)
    drawLine(suit, p(0.57f, 0.7f), p(0.63f, 0.88f), limbWidth, StrokeCap.Round)

    // Torso.
    drawRoundRect(
        color = suit,
        topLeft = p(0.34f, 0.42f),
        size = Size(0.32f * s, 0.3f * s),
        cornerRadius = CornerRadius(0.12f * s),
    )

    // Chest control panel.
    drawRoundRect(
        color = OddlyColors.Purple.copy(alpha = 0.55f),
        topLeft = p(0.43f, 0.5f),
        size = Size(0.14f * s, 0.09f * s),
        cornerRadius = CornerRadius(0.03f * s),
    )

    // Helmet shell and visor.
    drawCircle(suit, radius = 0.2f * s, center = p(0.5f, 0.28f))
    drawCircle(
        brush = visorBrush,
        radius = 0.145f * s,
        center = p(0.5f, 0.28f),
    )
    // Visor highlight.
    drawCircle(
        color = Color.White.copy(alpha = 0.55f),
        radius = 0.035f * s,
        center = p(0.44f, 0.22f),
    )
}

/** A ringed planet, used on empty states and the share card. */
@Composable
fun Planet(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    color: Color = OddlyColors.Purple,
) {
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension
        val center = Offset(s / 2f, s / 2f)

        drawCircle(
            brush = Brush.linearGradient(
                listOf(color, color.copy(alpha = 0.6f), OddlyColors.Indigo),
                start = Offset(0f, 0f),
                end = Offset(s, s),
            ),
            radius = 0.3f * s,
            center = center,
        )
        // Surface craters.
        drawCircle(Color.Black.copy(alpha = 0.13f), radius = 0.06f * s, center = Offset(s * 0.42f, s * 0.42f))
        drawCircle(Color.Black.copy(alpha = 0.1f), radius = 0.04f * s, center = Offset(s * 0.6f, s * 0.56f))

        // Tilted ring.
        rotate(degrees = -22f, pivot = center) {
            drawOval(
                color = OddlyColors.Pink.copy(alpha = 0.75f),
                topLeft = Offset(s * 0.08f, s * 0.42f),
                size = Size(s * 0.84f, s * 0.16f),
                style = Stroke(width = 0.035f * s),
            )
        }
    }
}

/**
 * The splash-screen ring: a sweep-gradient arc that rotates while the app boots.
 */
@Composable
fun BrandRing(
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    strokeWidth: Dp = 8.dp,
) {
    val transition = rememberInfiniteTransition(label = "ring")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ring-rotation",
    )

    Canvas(modifier.size(size)) {
        val s = this.size.minDimension
        val inset = strokeWidth.toPx() / 2f
        rotate(angle) {
            drawArc(
                brush = OddlyGradients.brandSweep,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(s - inset * 2, s - inset * 2),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
            )
        }
        // A bright bead riding the ring.
        val radians = (angle - 90f) * (kotlin.math.PI / 180f).toFloat()
        drawCircle(
            color = Color.White,
            radius = strokeWidth.toPx() * 0.55f,
            center = Offset(
                s / 2f + cos(radians) * (s / 2f - inset),
                s / 2f + sin(radians) * (s / 2f - inset),
            ),
        )
    }
}

/** Convenience wrapper: starfield behind arbitrary hero content. */
@Composable
fun StarryBox(
    modifier: Modifier = Modifier,
    starCount: Int = 40,
    seed: Int = 11,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier) {
        StarField(Modifier.fillMaxSize(), starCount = starCount, seed = seed)
        content()
    }
}
