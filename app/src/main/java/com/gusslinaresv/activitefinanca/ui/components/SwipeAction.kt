package com.gusslinaresv.activitefinanca.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

/**
 * Envuelve [content] para que pueda deslizarse horizontalmente. Si se suelta pasado el umbral,
 * se ejecuta [onTrigger] y la tarjeta regresa a su lugar con un rebote.
 */
@Composable
fun SwipeAction(
    icon: ImageVector,
    label: String,
    color: Color,
    onTrigger: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val offset = remember { Animatable(0f) }
    val currentOnTrigger by rememberUpdatedState(onTrigger)
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val threshold = with(LocalDensity.current) { 96.dp.toPx() }

    Box(modifier) {
        // Fondo que se revela al deslizar
        val progress = min(abs(offset.value) / threshold, 1f)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(20.dp))
                .background(color.copy(alpha = 0.15f + 0.35f * progress))
                .padding(horizontal = 24.dp),
        ) {
            if (offset.value < 0) Spacer(Modifier.weight(1f))
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.graphicsLayer { scaleX = 0.7f + 0.5f * progress; scaleY = scaleX })
            Spacer(Modifier.width(8.dp))
            Text(label, color = color, style = MaterialTheme.typography.labelLarge)
        }
        Box(
            Modifier
                .graphicsLayer { translationX = offset.value }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (abs(offset.value) >= threshold) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    currentOnTrigger()
                                }
                                offset.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            }
                        },
                        onDragCancel = { scope.launch { offset.animateTo(0f) } },
                    ) { change, dragAmount ->
                        change.consume()
                        scope.launch { offset.snapTo(offset.value + dragAmount * 0.8f) }
                    }
                }
        ) { content() }
    }
}
