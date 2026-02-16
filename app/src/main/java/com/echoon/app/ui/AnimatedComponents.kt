package com.echoon.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Animated floating blob for background atmosphere.
 * Use behind content so glass cards let it peek through.
 */
@Composable
fun FloatingBlob(
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    offsetX: Dp = 140.dp,
    offsetYStart: Dp = (-60).dp,
    floatAmount: Float = 30f,
    durationMs: Int = 4000,
    color: Color = Color(0xFFDCE775),
    alpha: Float = 0.3f,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = floatAmount,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "y_axis",
    )

    Box(
        modifier = modifier
            .size(size)
            .offset(x = offsetX, y = offsetYStart + floatAnim.dp)
            .background(color.copy(alpha = alpha), CircleShape),
    )
}

/**
 * Modifier for glassmorphism card border (specular highlight).
 * Apply after clip(shape) and before background/padding so border follows the shape.
 */
fun Modifier.glassBorder(
    shape: RoundedCornerShape,
    borderWidth: Dp = 1.dp,
    highlightAlpha: Float = 0.3f,
): Modifier = border(
    width = borderWidth,
    brush = Brush.linearGradient(
        colors = listOf(Color.White.copy(alpha = highlightAlpha), Color.Transparent),
    ),
    shape = shape,
)
