package com.sepehrpg.scaninsta.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp

@Composable
fun AppFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: Shape = FloatingActionButtonDefaults.shape,
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        shape = shape,
        elevation = elevation,
        interactionSource = interactionSource,
        content = content,
    )
}

@Composable
fun AppExtendedFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: Shape = FloatingActionButtonDefaults.extendedFabShape,
    expanded: Boolean = true,
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(),
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = elevation,
    ) {
        val startPadding = if (expanded) 10.dp else 0.dp
        val endPadding = if (expanded) 10.dp else 0.dp
        Row(
            modifier = Modifier
                .sizeIn(minWidth = if (expanded) 80.dp else 56.dp)
                .padding(start = startPadding, end = endPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (expanded) Arrangement.Start else Arrangement.Center,
        ) {
            icon()
            AnimatedVisibility(
                visible = expanded,
                enter = extendedFabExpandAnimation,
                exit = extendedFabCollapseAnimation,
            ) {
                Row(Modifier.clearAndSetSemantics {}) {
                    Spacer(Modifier.width(5.dp))
                    text()
                }
            }
        }
    }
}

private val extendedFabCollapseAnimation = fadeOut(
    animationSpec = tween(
        durationMillis = 100,
        easing = CubicBezierEasing(0f, 0f, 1f, 1f),
    ),
) + shrinkHorizontally(
    animationSpec = tween(
        durationMillis = 500,
        easing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
    ),
    shrinkTowards = Alignment.Start,
)

private val extendedFabExpandAnimation = fadeIn(
    animationSpec = tween(
        durationMillis = 200,
        delayMillis = 100,
        easing = CubicBezierEasing(0f, 0f, 1f, 1f),
    ),
) + expandHorizontally(
    animationSpec = tween(
        durationMillis = 500,
        easing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
    ),
    expandFrom = Alignment.Start,
)
