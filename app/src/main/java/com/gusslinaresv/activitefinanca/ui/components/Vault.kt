package com.gusslinaresv.activitefinanca.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.gusslinaresv.activitefinanca.data.BanknoteRepository
import com.gusslinaresv.activitefinanca.data.model.Currency
import kotlin.math.cos
import kotlin.math.sin

/** Paleta metálica de la bóveda, adaptada a tema claro/oscuro. */
private data class Steel(val light: Color, val mid: Color, val dark: Color, val wall: Color, val rivet: Color)

@Composable
private fun steel(): Steel = if (MaterialTheme.colorScheme.background.luminance() > 0.5f) {
    Steel(Color(0xFFE3E7EB), Color(0xFFB9C1C9), Color(0xFF8C96A1), Color(0xFF6B7682), Color(0xFF5A636D))
} else {
    Steel(Color(0xFF5B6674), Color(0xFF414B58), Color(0xFF2C343F), Color(0xFF1A212B), Color(0xFF11161D))
}

private const val COLUMNS = 3

/**
 * Muro de cajas de seguridad. [zoom] (0..1) acerca la "cámara" a la caja [selected],
 * y [doorOpen] (0..1) abre su puerta.
 */
@Composable
fun VaultWall(
    currencies: List<Currency>,
    featuredCode: String,
    learned: Set<String>,
    selected: Int?,
    zoom: Float,
    doorOpen: Float,
    onBoxTap: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = steel()
    val rows = (currencies.size + COLUMNS - 1) / COLUMNS
    BoxWithConstraints(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.verticalGradient(listOf(s.wall, s.rivet)))
    ) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()

        // Remaches del muro
        Canvas(Modifier.fillMaxSize()) {
            val step = 28.dp.toPx()
            var y = step / 2
            while (y < size.height) {
                var x = step / 2
                while (x < size.width) {
                    drawCircle(Color.White.copy(alpha = 0.07f), radius = 2.dp.toPx(), center = Offset(x, y))
                    x += step
                }
                y += step
            }
        }

        // Centro de la caja elegida, para dirigir la cámara hacia ella
        val sel = selected ?: 0
        val cx = ((sel % COLUMNS) + 0.5f) / COLUMNS * w
        val cy = ((sel / COLUMNS) + 0.5f) / rows * h

        Column(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val scale = 1f + 1.7f * zoom
                    transformOrigin = TransformOrigin(cx / w, cy / h)
                    scaleX = scale
                    scaleY = scale
                    translationX = (w / 2 - cx) * zoom
                    translationY = (h / 2 - cy) * zoom
                }
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (r in 0 until rows) {
                Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    for (c in 0 until COLUMNS) {
                        val i = r * COLUMNS + c
                        if (i >= currencies.size) {
                            Box(Modifier.weight(1f))
                            continue
                        }
                        val currency = currencies[i]
                        DepositBox(
                            currency = currency,
                            featured = currency.code == featuredCode,
                            learned = currency.code in learned,
                            door = if (selected == i) doorOpen else 0f,
                            dimmed = selected != null && selected != i,
                            onClick = { onBoxTap(i) },
                            modifier = Modifier.weight(1f).fillMaxSize().zIndex(if (selected == i) 1f else 0f)
                        )
                    }
                }
            }
        }
    }
}

/** Una caja de seguridad: interior con el billete asomando y una puerta de acero que se abre. */
@Composable
private fun DepositBox(
    currency: Currency,
    featured: Boolean,
    learned: Boolean,
    door: Float,
    dimmed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier,
) {
    val s = steel()
    val gold = Color(0xFFF6C453)
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val glow by rememberInfiniteTransition(label = "featured").animateFloat(
        0.35f, 1f, infiniteRepeatable(tween(1100), RepeatMode.Reverse), label = "glow"
    )
    val noteColor = remember(currency) { BanknoteRepository.get(currency.code).color }

    Box(
        modifier
            .graphicsLayer {
                val p = if (pressed) 0.94f else 1f
                scaleX = p
                scaleY = p
                alpha = if (dimmed) 0.55f else 1f
            }
            .shadow(6.dp, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        // Interior oscuro con luz dorada y el billete dentro
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.radialGradient(listOf(Color(0xFF3A2F12), Color(0xFF0D0D0D))))
        ) {
            Canvas(Modifier.fillMaxSize().padding(14.dp)) {
                drawRoundRect(
                    noteColor, topLeft = Offset(size.width * 0.1f, size.height * 0.35f),
                    size = Size(size.width * 0.8f, size.height * 0.4f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                )
            }
        }

        // Puerta de acero: gira sobre su bisagra izquierda
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0.5f)
                    rotationY = -105f * door
                    cameraDistance = 10 * density
                }
                .background(Brush.linearGradient(listOf(s.light, s.mid, s.dark)))
                .border(
                    if (featured) BorderStroke(3.dp, gold.copy(alpha = glow)) else BorderStroke(1.dp, Color.Black.copy(alpha = 0.25f)),
                    RoundedCornerShape(12.dp)
                )
        ) {
            // Tornillos en las esquinas y cerradura
            Canvas(Modifier.fillMaxSize()) {
                val m = 8.dp.toPx()
                listOf(Offset(m, m), Offset(size.width - m, m), Offset(m, size.height - m), Offset(size.width - m, size.height - m)).forEach {
                    drawCircle(s.dark, radius = 2.5.dp.toPx(), center = it)
                    drawCircle(Color.White.copy(alpha = 0.5f), radius = 1.dp.toPx(), center = it - Offset(0.7f, 0.7f))
                }
                val k = Offset(size.width - 18.dp.toPx(), size.height - 20.dp.toPx())
                drawCircle(s.dark, radius = 7.dp.toPx(), center = k)
                drawCircle(Color.Black.copy(alpha = 0.7f), radius = 2.5.dp.toPx(), center = k)
                drawRect(Color.Black.copy(alpha = 0.7f), topLeft = Offset(k.x - 1.dp.toPx(), k.y), size = Size(2.dp.toPx(), 5.dp.toPx()))
            }
            FlagBadge(currency, size = 22.dp, modifier = Modifier.align(Alignment.TopStart).padding(8.dp))
            // Símbolo grabado en el acero
            Text(
                currency.symbol,
                modifier = Modifier.align(Alignment.Center),
                style = TextStyle(
                    fontSize = if (currency.symbol.length > 2) 26.sp else 40.sp,
                    fontWeight = FontWeight.Black,
                    color = s.dark,
                    shadow = Shadow(Color.White.copy(alpha = 0.7f), Offset(1.5f, 1.5f), 1f)
                )
            )
            Text(
                currency.code,
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 9.dp, bottom = 7.dp),
                style = MaterialTheme.typography.labelLarge.copy(color = s.dark, fontWeight = FontWeight.Bold)
            )
            if (learned) Text("🏅", modifier = Modifier.align(Alignment.TopEnd).padding(6.dp))
            if (featured) {
                Text(
                    "HOY",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = if (learned) 30.dp else 6.dp, end = 6.dp)
                        .background(gold, RoundedCornerShape(50))
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF4A3000), fontWeight = FontWeight.Black)
                )
            }
        }
    }
}

/**
 * Puerta circular de la bóveda: el volante gira ([wheel] 0..1) y luego la puerta se abre ([open] 0..1)
 * como si girara sobre sus bisagras.
 */
@Composable
fun VaultDoor(wheel: Float, open: Float, modifier: Modifier = Modifier) {
    val s = steel()
    val gold = Color(0xFFF6C453)
    Box(
        modifier
            .graphicsLayer {
                transformOrigin = TransformOrigin(0f, 0.5f)
                rotationY = -100f * open
                alpha = 1f - open * 0.8f
                cameraDistance = 8 * density
            }
            .background(Brush.radialGradient(listOf(s.mid, s.dark, s.rivet))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center
            val r = size.minDimension * 0.42f
            // Aros concéntricos de acero
            drawCircle(Brush.radialGradient(listOf(s.light, s.mid, s.dark), c, r), radius = r, center = c)
            drawCircle(s.dark, radius = r, center = c, style = Stroke(10.dp.toPx()))
            drawCircle(s.dark.copy(alpha = 0.6f), radius = r * 0.78f, center = c, style = Stroke(3.dp.toPx()))
            // Pernos alrededor
            for (i in 0 until 12) {
                val a = i / 12f * 2 * Math.PI
                val p = Offset(c.x + cos(a).toFloat() * r * 0.9f, c.y + sin(a).toFloat() * r * 0.9f)
                drawCircle(s.dark, radius = 7.dp.toPx(), center = p)
                drawCircle(Color.White.copy(alpha = 0.4f), radius = 2.dp.toPx(), center = p - Offset(2f, 2f))
            }
            // Volante con 3 brazos que gira
            rotate(degrees = wheel * 360f, pivot = c) {
                for (i in 0 until 3) {
                    val a = i / 3f * 2 * Math.PI
                    val end = Offset(c.x + cos(a).toFloat() * r * 0.55f, c.y + sin(a).toFloat() * r * 0.55f)
                    drawLine(s.dark, c, end, strokeWidth = 12.dp.toPx())
                    drawCircle(gold, radius = 10.dp.toPx(), center = end)
                }
                drawCircle(gold, radius = r * 0.16f, center = c)
                drawCircle(s.dark, radius = r * 0.16f, center = c, style = Stroke(3.dp.toPx()))
            }
        }
        Text(
            "BÓVEDA DE DIVISAS",
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp),
            style = MaterialTheme.typography.titleLarge.copy(color = gold, fontWeight = FontWeight.Black)
        )
    }
}
