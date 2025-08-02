package com.sepehrpg.scaninsta.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 1.dp,
    cornerRadius: Dp = 0.dp,
    dashOn: Dp = 10.dp,
    dashOff: Dp = 5.dp
): Modifier = composed {
    // Convert Dp values to Px for drawing
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }
    val dashOnPx = with(density) { dashOn.toPx() }
    val dashOffPx = with(density) { dashOff.toPx() }

    // Create the Stroke object with the dash effect
    val stroke = Stroke(
        width = strokeWidthPx,
        pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(dashOnPx, dashOffPx),
            phase = 0f
        )
    )

    this.drawBehind {
        drawRoundRect(
            color = color,
            style = stroke,
            cornerRadius = CornerRadius(cornerRadiusPx)
        )
    }
}