package dev.lildua.oddly.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.lildua.oddly.ui.theme.OddlyTheme

/**
 * The app's icon set, drawn with [Canvas] primitives instead of pulling in a
 * Material icon font or shipping vector assets.
 *
 * Decorative icons (categories, celebrations) use emoji — see [Category.emoji].
 * These are the UI-chrome icons: navigation, chevrons, settings rows.
 *
 * All shapes are authored in a normalised 0..1 box and scaled to the requested
 * size, so they stay crisp at any dimension.
 */
enum class OddlyIcon {
    ChevronRight,
    ChevronLeft,
    ChevronDown,
    Check,
    Close,
    Home,
    Journey,
    Stats,
    Settings,
    Share,
    Bell,
    Plus,
    Calendar,
    Flame,
    Sparkle,
    Heart,
    Globe,
    Download,
    Trash,
    Info,
    Palette,
    Volume,
    Clock,
    Refresh,
    Target,
    Dice,
}

@Composable
fun OddlyIcon(
    icon: OddlyIcon,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
    tint: Color = OddlyTheme.palette.textPrimary,
    strokeWidth: Dp = 1.75.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        drawOddlyIcon(icon, this.size.minDimension, tint, stroke)
    }
}

private fun DrawScope.drawOddlyIcon(icon: OddlyIcon, s: Float, tint: Color, stroke: Stroke) {
    fun p(x: Float, y: Float) = Offset(x * s, y * s)
    fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
        drawLine(tint, p(x1, y1), p(x2, y2), stroke.width, StrokeCap.Round)

    fun poly(vararg points: Pair<Float, Float>) {
        val path = Path().apply {
            moveTo(points[0].first * s, points[0].second * s)
            points.drop(1).forEach { lineTo(it.first * s, it.second * s) }
        }
        drawPath(path, tint, style = stroke)
    }

    when (icon) {
        OddlyIcon.ChevronRight -> poly(0.38f to 0.24f, 0.66f to 0.5f, 0.38f to 0.76f)
        OddlyIcon.ChevronLeft -> poly(0.62f to 0.24f, 0.34f to 0.5f, 0.62f to 0.76f)
        OddlyIcon.ChevronDown -> poly(0.24f to 0.4f, 0.5f to 0.66f, 0.76f to 0.4f)

        OddlyIcon.Check -> poly(0.22f to 0.52f, 0.42f to 0.72f, 0.78f to 0.3f)

        OddlyIcon.Close -> {
            line(0.27f, 0.27f, 0.73f, 0.73f)
            line(0.73f, 0.27f, 0.27f, 0.73f)
        }

        OddlyIcon.Home -> {
            poly(0.15f to 0.48f, 0.5f to 0.18f, 0.85f to 0.48f)
            poly(
                0.25f to 0.44f,
                0.25f to 0.82f,
                0.75f to 0.82f,
                0.75f to 0.44f,
            )
        }

        // A winding path with a start dot and a destination pin.
        OddlyIcon.Journey -> {
            val path = Path().apply {
                moveTo(0.24f * s, 0.78f * s)
                cubicTo(
                    0.24f * s, 0.56f * s,
                    0.72f * s, 0.64f * s,
                    0.72f * s, 0.42f * s,
                )
            }
            drawPath(path, tint, style = stroke)
            drawCircle(tint, radius = 0.09f * s, center = p(0.24f, 0.8f))
            drawCircle(tint, radius = 0.11f * s, center = p(0.72f, 0.28f), style = stroke)
        }

        OddlyIcon.Stats -> {
            line(0.26f, 0.76f, 0.26f, 0.5f)
            line(0.5f, 0.76f, 0.5f, 0.26f)
            line(0.74f, 0.76f, 0.74f, 0.42f)
        }

        // Circle plus radial teeth reads as a gear at small sizes.
        OddlyIcon.Settings -> {
            drawCircle(tint, radius = 0.2f * s, center = center, style = stroke)
            repeat(8) { i ->
                val angle = (i * 45f) * (kotlin.math.PI / 180f).toFloat()
                val cos = kotlin.math.cos(angle)
                val sin = kotlin.math.sin(angle)
                drawLine(
                    tint,
                    Offset(center.x + cos * 0.28f * s, center.y + sin * 0.28f * s),
                    Offset(center.x + cos * 0.38f * s, center.y + sin * 0.38f * s),
                    stroke.width,
                    StrokeCap.Round,
                )
            }
        }

        OddlyIcon.Share -> {
            drawCircle(tint, radius = 0.1f * s, center = p(0.72f, 0.22f), style = stroke)
            drawCircle(tint, radius = 0.1f * s, center = p(0.28f, 0.5f), style = stroke)
            drawCircle(tint, radius = 0.1f * s, center = p(0.72f, 0.78f), style = stroke)
            line(0.37f, 0.44f, 0.63f, 0.28f)
            line(0.37f, 0.56f, 0.63f, 0.72f)
        }

        OddlyIcon.Bell -> {
            val dome = Path().apply {
                moveTo(0.26f * s, 0.66f * s)
                lineTo(0.26f * s, 0.46f * s)
                cubicTo(
                    0.26f * s, 0.26f * s,
                    0.74f * s, 0.26f * s,
                    0.74f * s, 0.46f * s,
                )
                lineTo(0.74f * s, 0.66f * s)
                close()
            }
            drawPath(dome, tint, style = stroke)
            line(0.18f, 0.66f, 0.82f, 0.66f)
            drawCircle(tint, radius = 0.06f * s, center = p(0.5f, 0.8f))
        }

        OddlyIcon.Plus -> {
            line(0.5f, 0.24f, 0.5f, 0.76f)
            line(0.24f, 0.5f, 0.76f, 0.5f)
        }

        OddlyIcon.Calendar -> {
            drawRoundRect(
                tint,
                topLeft = p(0.18f, 0.26f),
                size = Size(0.64f * s, 0.56f * s),
                cornerRadius = CornerRadius(0.1f * s),
                style = stroke,
            )
            line(0.18f, 0.44f, 0.82f, 0.44f)
            line(0.34f, 0.18f, 0.34f, 0.3f)
            line(0.66f, 0.18f, 0.66f, 0.3f)
        }

        OddlyIcon.Flame -> {
            val flame = Path().apply {
                moveTo(0.5f * s, 0.16f * s)
                cubicTo(0.74f * s, 0.38f * s, 0.82f * s, 0.56f * s, 0.72f * s, 0.7f * s)
                cubicTo(0.64f * s, 0.82f * s, 0.36f * s, 0.82f * s, 0.28f * s, 0.7f * s)
                cubicTo(0.18f * s, 0.56f * s, 0.3f * s, 0.42f * s, 0.42f * s, 0.34f * s)
                cubicTo(0.42f * s, 0.46f * s, 0.48f * s, 0.5f * s, 0.5f * s, 0.44f * s)
                close()
            }
            drawPath(flame, tint)
        }

        OddlyIcon.Sparkle -> {
            val star = Path().apply {
                moveTo(0.5f * s, 0.12f * s)
                cubicTo(0.56f * s, 0.4f * s, 0.6f * s, 0.44f * s, 0.88f * s, 0.5f * s)
                cubicTo(0.6f * s, 0.56f * s, 0.56f * s, 0.6f * s, 0.5f * s, 0.88f * s)
                cubicTo(0.44f * s, 0.6f * s, 0.4f * s, 0.56f * s, 0.12f * s, 0.5f * s)
                cubicTo(0.4f * s, 0.44f * s, 0.44f * s, 0.4f * s, 0.5f * s, 0.12f * s)
                close()
            }
            drawPath(star, tint)
        }

        OddlyIcon.Heart -> {
            val heart = Path().apply {
                moveTo(0.5f * s, 0.8f * s)
                cubicTo(0.1f * s, 0.55f * s, 0.18f * s, 0.22f * s, 0.5f * s, 0.36f * s)
                cubicTo(0.82f * s, 0.22f * s, 0.9f * s, 0.55f * s, 0.5f * s, 0.8f * s)
                close()
            }
            drawPath(heart, tint)
        }

        OddlyIcon.Globe -> {
            drawCircle(tint, radius = 0.32f * s, center = center, style = stroke)
            line(0.18f, 0.5f, 0.82f, 0.5f)
            val meridian = Path().apply {
                moveTo(0.5f * s, 0.18f * s)
                cubicTo(0.28f * s, 0.34f * s, 0.28f * s, 0.66f * s, 0.5f * s, 0.82f * s)
                cubicTo(0.72f * s, 0.66f * s, 0.72f * s, 0.34f * s, 0.5f * s, 0.18f * s)
                close()
            }
            drawPath(meridian, tint, style = stroke)
        }

        OddlyIcon.Download -> {
            line(0.5f, 0.18f, 0.5f, 0.6f)
            poly(0.32f to 0.44f, 0.5f to 0.62f, 0.68f to 0.44f)
            line(0.22f, 0.78f, 0.78f, 0.78f)
        }

        OddlyIcon.Trash -> {
            line(0.18f, 0.3f, 0.82f, 0.3f)
            line(0.4f, 0.3f, 0.4f, 0.2f)
            line(0.6f, 0.3f, 0.6f, 0.2f)
            line(0.4f, 0.2f, 0.6f, 0.2f)
            poly(0.28f to 0.3f, 0.33f to 0.82f, 0.67f to 0.82f, 0.72f to 0.3f)
            line(0.44f, 0.42f, 0.46f, 0.7f)
            line(0.56f, 0.42f, 0.54f, 0.7f)
        }

        OddlyIcon.Info -> {
            drawCircle(tint, radius = 0.32f * s, center = center, style = stroke)
            drawCircle(tint, radius = 0.045f * s, center = p(0.5f, 0.32f))
            line(0.5f, 0.45f, 0.5f, 0.7f)
        }

        OddlyIcon.Palette -> {
            drawCircle(tint, radius = 0.32f * s, center = center, style = stroke)
            drawCircle(tint, radius = 0.055f * s, center = p(0.38f, 0.36f))
            drawCircle(tint, radius = 0.055f * s, center = p(0.62f, 0.36f))
            drawCircle(tint, radius = 0.055f * s, center = p(0.34f, 0.6f))
            drawCircle(tint, radius = 0.055f * s, center = p(0.58f, 0.66f))
        }

        OddlyIcon.Volume -> {
            poly(
                0.2f to 0.4f,
                0.34f to 0.4f,
                0.5f to 0.24f,
                0.5f to 0.76f,
                0.34f to 0.6f,
                0.2f to 0.6f,
            )
            drawArc(
                color = tint,
                startAngle = -50f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = p(0.42f, 0.3f),
                size = Size(0.34f * s, 0.4f * s),
                style = stroke,
            )
            drawArc(
                color = tint,
                startAngle = -50f,
                sweepAngle = 100f,
                useCenter = false,
                topLeft = p(0.46f, 0.2f),
                size = Size(0.5f * s, 0.6f * s),
                style = stroke,
            )
        }

        OddlyIcon.Clock -> {
            drawCircle(tint, radius = 0.32f * s, center = center, style = stroke)
            line(0.5f, 0.5f, 0.5f, 0.3f)
            line(0.5f, 0.5f, 0.64f, 0.58f)
        }

        OddlyIcon.Refresh -> {
            drawArc(
                color = tint,
                startAngle = 40f,
                sweepAngle = 280f,
                useCenter = false,
                topLeft = p(0.2f, 0.2f),
                size = Size(0.6f * s, 0.6f * s),
                style = stroke,
            )
            poly(0.66f to 0.52f, 0.76f to 0.72f, 0.9f to 0.56f)
        }

        OddlyIcon.Target -> {
            drawCircle(tint, radius = 0.32f * s, center = center, style = stroke)
            drawCircle(tint, radius = 0.18f * s, center = center, style = stroke)
            drawCircle(tint, radius = 0.06f * s, center = center)
        }

        OddlyIcon.Dice -> {
            drawRoundRect(
                tint,
                topLeft = p(0.16f, 0.16f),
                size = Size(0.68f * s, 0.68f * s),
                cornerRadius = CornerRadius(0.16f * s),
                style = stroke,
            )
            listOf(
                0.33f to 0.33f,
                0.67f to 0.33f,
                0.5f to 0.5f,
                0.33f to 0.67f,
                0.67f to 0.67f,
            ).forEach { (x, y) ->
                drawCircle(tint, radius = 0.055f * s, center = p(x, y))
            }
        }
    }
}
