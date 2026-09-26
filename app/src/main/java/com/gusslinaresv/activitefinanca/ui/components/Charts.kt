package com.gusslinaresv.activitefinanca.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.gusslinaresv.activitefinanca.ui.theme.NumberStyle

/** Minigráfica sin ejes para las tarjetas. */
@Composable
fun Sparkline(values: List<Double>, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val min = values.min()
        val max = values.max()
        val range = (max - min).takeIf { it > 0 } ?: 1.0
        val stepX = size.width / (values.size - 1)
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - ((v - min) / range * size.height).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

/**
 * Gráfica de línea interactiva: se dibuja con una animación de izquierda a derecha y,
 * al tocar o arrastrar el dedo, muestra el valor del mes seleccionado.
 */
@Composable
fun InteractiveLineChart(
    values: List<Double>,
    labels: List<String>,
    lineColor: Color,
    valueFormatter: (Double) -> String,
    modifier: Modifier = Modifier,
) {
    var selected by remember(values) { mutableStateOf(values.lastIndex) }
    val progress = remember(values) { Animatable(0f) }
    LaunchedEffect(values) { progress.animateTo(1f, tween(1100, easing = FastOutSlowInEasing)) }

    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val pointFill = MaterialTheme.colorScheme.surface

    Column(modifier) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Text(
                valueFormatter(values[selected]),
                style = MaterialTheme.typography.headlineSmall.merge(NumberStyle),
                modifier = Modifier.weight(1f)
            )
            Text(
                labels[selected],
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(values) {
                    fun pick(x: Float) {
                        val step = size.width.toFloat() / (values.size - 1)
                        selected = (x / step).let { kotlin.math.round(it).toInt() }.coerceIn(0, values.lastIndex)
                    }
                    detectTapGestures { pick(it.x) }
                }
                .pointerInput(values) {
                    // Solo arrastre horizontal: el vertical se deja al scroll de la pantalla
                    detectHorizontalDragGestures { change, _ ->
                        val step = size.width.toFloat() / (values.size - 1)
                        selected = kotlin.math.round(change.position.x / step).toInt().coerceIn(0, values.lastIndex)
                    }
                }
        ) {
            val min = values.min()
            val max = values.max()
            val range = (max - min).takeIf { it > 0 } ?: 1.0
            val top = 12.dp.toPx()
            val usable = size.height - top * 2
            val stepX = size.width / (values.size - 1)
            fun point(i: Int) = Offset(i * stepX, top + usable - ((values[i] - min) / range * usable).toFloat())

            // Líneas guía horizontales
            repeat(4) { i ->
                val y = top + usable * i / 3f
                drawLine(gridColor, Offset(0f, y), Offset(size.width, y), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
            }

            val line = Path()
            val area = Path()
            values.indices.forEach { i ->
                val p = point(i)
                if (i == 0) {
                    line.moveTo(p.x, p.y); area.moveTo(p.x, size.height); area.lineTo(p.x, p.y)
                } else {
                    val prev = point(i - 1)
                    val midX = (prev.x + p.x) / 2
                    line.cubicTo(midX, prev.y, midX, p.y, p.x, p.y)
                    area.cubicTo(midX, prev.y, midX, p.y, p.x, p.y)
                }
            }
            area.lineTo(size.width, size.height)
            area.close()

            clipRect(right = size.width * progress.value) {
                drawPath(area, Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.28f), Color.Transparent)))
                drawPath(line, lineColor, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
            }

            // Indicador del punto seleccionado
            val sel = point(selected)
            drawLine(lineColor.copy(alpha = 0.5f), Offset(sel.x, 0f), Offset(sel.x, size.height), strokeWidth = 1.5.dp.toPx())
            drawCircle(lineColor, radius = 7.dp.toPx(), center = sel)
            drawCircle(pointFill, radius = 3.5.dp.toPx(), center = sel)
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
            labels.forEachIndexed { i, label ->
                Text(
                    if (i % 2 == 0 || i == labels.lastIndex) label else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (i == selected) lineColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
