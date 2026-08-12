package com.ivor.scale.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.ivor.scale.ui.theme.rememberRobotoFlexAxis
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Springy press feedback: the element squishes slightly when pressed and bounces
 * back. Pairs with [MotionScheme.expressive] for a consistent tactile feel where
 * alpha16 doesn't yet expose `Button(shapes = …)` shape-morphing.
 */
fun Modifier.expressivePress(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.94f,
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 650f),
        label = "expressivePressScale",
    )
    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * The hero result number. Uses Roboto Flex's weight & width variation axes to
 * give a subtle expressive "morph" each time the value changes — the digits
 * settle into place with a spring instead of snapping. Kept gentle so it stays
 * pleasant during live typing.
 */
@Composable
fun HeroNumeral(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val morph = remember { Animatable(1f) }
    LaunchedEffect(text) {
        morph.snapTo(0f)
        morph.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 320f),
        )
    }
    val p = morph.value.coerceIn(0f, 1f)
    // From a touch lighter & wider, settling into heavy & normal-width.
    val weightAxis = ((840 + 60 * p).roundToInt() / 10) * 10
    val widthAxis = 112f - 12f * p
    val family = rememberRobotoFlexAxis(weightAxis, widthAxis)

    Text(
        text = text,
        style = style,
        color = color,
        fontFamily = family,
        modifier = modifier,
    )
}

/**
 * A soft scalloped "cookie" outline — a circle whose radius is gently rippled into
 * [lobes] rounded bumps. One of the signature M3 Expressive silhouettes; gives an
 * icon badge a friendlier, more crafted feel than a plain circle without reaching
 * for the alpha `MaterialShapes` API.
 */
class ScallopedShape(
    private val lobes: Int = 8,
    private val depth: Float = 0.14f,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val base = min(cx, cy)
        val rMid = base * (1f - depth / 2f)
        val amp = base * depth / 2f
        val steps = 240
        val path = Path()
        for (i in 0..steps) {
            val angle = (i.toFloat() / steps * 2f * PI - PI / 2f).toFloat()
            val r = rMid + amp * cos(lobes * angle)
            val x = cx + r * cos(angle)
            val y = cy + r * sin(angle)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}
