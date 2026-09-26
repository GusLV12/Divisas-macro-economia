package com.gusslinaresv.activitefinanca.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.gusslinaresv.activitefinanca.data.model.Currency
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** Estado físico de una burbuja. x/y son estado de Compose para que solo se re-ejecute el layout al moverse. */
private class Bubble(val currency: Currency, val radius: Float, startX: Float, startY: Float, val phase: Float) {
    var x by mutableFloatStateOf(startX)
    var y by mutableFloatStateOf(startY)
    var vx = 0f
    var vy = 0f
    var dragging = false
    val scale = Animatable(0f)
    val expand = Animatable(1f)
    val active: Boolean get() = scale.targetValue > 0.5f
}

/**
 * Campo de burbujas flotantes: cada divisa es una burbuja cuyo tamaño refleja cuánto se negocia en el mundo.
 * Flotan atraídas suavemente al centro, chocan entre sí, se pueden arrastrar y lanzar.
 * Al tocar una, las demás salen despedidas, la burbuja se expande y se llama a [onSelect].
 */
@Composable
fun CurrencyBubbleField(
    currencies: List<Currency>,
    visibleCodes: Set<String>,
    learned: Set<String>,
    onSelect: (Currency) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val currentOnSelect by rememberUpdatedState(onSelect)

    BoxWithConstraints(modifier) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val time = remember { mutableFloatStateOf(0f) }
        var selected by remember { mutableStateOf<Bubble?>(null) }

        val bubbles = remember(currencies, w, h) {
            val maxShare = currencies.maxOf { it.tradeShare }
            val maxR = minOf(w, h) * 0.2f
            val minR = minOf(w, h) * 0.085f
            val random = Random(42)
            currencies.map { c ->
                // Raíz cuadrada: el ÁREA de la burbuja es proporcional al volumen negociado
                val r = minR + (maxR - minR) * sqrt((c.tradeShare / maxShare).toFloat())
                Bubble(
                    currency = c,
                    radius = r,
                    startX = w / 2 + (random.nextFloat() - 0.5f) * w * 0.8f,
                    startY = h / 2 + (random.nextFloat() - 0.5f) * h * 0.8f,
                    phase = random.nextFloat() * 6.28f,
                )
            }
        }

        // Entrada escalonada y aparición/desaparición según la búsqueda y los filtros
        LaunchedEffect(bubbles, visibleCodes) {
            bubbles.forEachIndexed { i, b ->
                launch {
                    val show = b.currency.code in visibleCodes
                    if (show && b.scale.value == 0f) delay(i * 45L)
                    b.scale.animateTo(
                        if (show) 1f else 0f,
                        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                    )
                }
            }
        }

        // Bucle de física: se ejecuta en cada cuadro de pantalla
        LaunchedEffect(bubbles) {
            var last = 0L
            while (true) {
                withFrameNanos { now ->
                    val dt = if (last == 0L) 0f else ((now - last) / 1e9f).coerceAtMost(1f / 30f)
                    last = now
                    time.floatValue += dt
                    step(bubbles, dt, time.floatValue, w, h, density.density)
                }
            }
        }

        bubbles.forEach { b ->
            key(b.currency.code) {
                BubbleView(
                    bubble = b,
                    time = { time.floatValue },
                    isLearned = b.currency.code in learned,
                    isSelected = selected === b,
                    onTap = {
                        if (selected != null) return@BubbleView
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        selected = b
                        scope.launch {
                            // Las demás burbujas salen despedidas desde la elegida
                            bubbles.filter { it !== b && it.active }.forEach { o ->
                                val dx = o.x - b.x
                                val dy = o.y - b.y
                                val d = max(hypot(dx, dy), 1f)
                                o.vx += dx / d * 540f * density.density
                                o.vy += dy / d * 540f * density.density
                            }
                            b.expand.animateTo(1.25f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                            // Crece hasta cubrir toda la pantalla, como un "portal" a la lección
                            b.expand.animateTo(hypot(w, h) / b.radius, tween(420, easing = FastOutSlowInEasing))
                            currentOnSelect(b.currency)
                            delay(700)
                            b.expand.snapTo(1f)
                            selected = null
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BubbleView(
    bubble: Bubble,
    time: () -> Float,
    isLearned: Boolean,
    isSelected: Boolean,
    onTap: () -> Unit,
) {
    val density = LocalDensity.current
    val sizeDp = with(density) { (bubble.radius * 2).toDp() }
    val tracker = remember { VelocityTracker() }
    val gold = MaterialTheme.colorScheme.tertiary
    val portal = MaterialTheme.colorScheme.background

    Box(
        Modifier
            .offset { IntOffset((bubble.x - bubble.radius).roundToInt(), (bubble.y - bubble.radius).roundToInt()) }
            .zIndex(if (isSelected) 2f else if (bubble.dragging) 1f else 0f)
            .size(sizeDp)
            .graphicsLayer {
                // "Respiración": cada burbuja late levemente con su propia fase
                val breathe = 1f + 0.03f * sin(time() * 1.6f + bubble.phase)
                val s = bubble.scale.value * bubble.expand.value * breathe
                scaleX = s
                scaleY = s
                alpha = bubble.scale.value.coerceIn(0f, 1f)
            }
            .shadow(if (isSelected) 0.dp else 8.dp, CircleShape)
            .clip(CircleShape)
            .border(
                if (isLearned) BorderStroke(3.dp, gold) else BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                CircleShape
            )
            .pointerInput(bubble) { detectTapGestures(onTap = { onTap() }) }
            .pointerInput(bubble) {
                detectDragGestures(
                    onDragStart = {
                        bubble.dragging = true
                        tracker.resetTracking()
                    },
                    onDragEnd = {
                        // Al soltar, la burbuja conserva la velocidad del dedo: se puede "lanzar"
                        val v = tracker.calculateVelocity()
                        bubble.vx = v.x.coerceIn(-3000f, 3000f)
                        bubble.vy = v.y.coerceIn(-3000f, 3000f)
                        bubble.dragging = false
                    },
                    onDragCancel = { bubble.dragging = false },
                ) { change, drag ->
                    change.consume()
                    bubble.x += drag.x
                    bubble.y += drag.y
                    tracker.addPosition(change.uptimeMillis, Offset(bubble.x, bubble.y))
                }
            }
    ) {
        Image(
            painterResource(bubble.currency.flagRes),
            contentDescription = bubble.currency.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Brillo superior para dar volumen de burbuja y degradado inferior para leer el código
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                        center = Offset(bubble.radius * 0.65f, bubble.radius * 0.55f),
                        radius = bubble.radius * 0.9f
                    )
                )
                .background(Brush.verticalGradient(0.7f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.35f)))
        )
        // Código en una pastilla pequeña al pie para no tapar el escudo o dibujo de la bandera
        Text(
            bubble.currency.code,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = with(density) { (bubble.radius * 0.22f).toSp() },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = with(density) { (bubble.radius * 0.1f).toDp() })
                .background(Color.Black.copy(alpha = 0.35f), CircleShape)
                .padding(horizontal = with(density) { (bubble.radius * 0.12f).toDp() })
        )
        // Al expandirse, un color sólido cubre la bandera para que no se vea pixelada al crecer
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = ((bubble.expand.value - 1.3f) / 1.2f).coerceIn(0f, 1f) }
                .background(portal)
        )
        if (isLearned) {
            Text(
                "🏅",
                fontSize = with(density) { (bubble.radius * 0.3f).toSp() },
                modifier = Modifier.align(Alignment.TopCenter).padding(top = with(density) { (bubble.radius * 0.12f).toDp() })
            )
        }
    }
}

/** Integra un paso de simulación: atracción al centro, deriva, amortiguación, choques y paredes. */
private fun step(bubbles: List<Bubble>, dt: Float, time: Float, w: Float, h: Float, density: Float) {
    if (dt == 0f) return
    val active = bubbles.filter { it.active }
    val cx = w / 2
    val cy = h / 2
    val damping = 1f - (1.6f * dt).coerceAtMost(0.5f)

    for (b in active) {
        if (b.dragging) continue
        // Atracción suave hacia el centro para que se agrupen
        b.vx += (cx - b.x) * 0.9f * dt
        b.vy += (cy - b.y) * 0.9f * dt
        // Deriva tipo "flotando en el agua"
        b.vx += cos(time * 0.8f + b.phase) * 38f * density * dt
        b.vy += sin(time * 0.6f + b.phase * 1.3f) * 38f * density * dt
        b.vx *= damping
        b.vy *= damping
    }

    // Choques entre burbujas
    val gap = 4f * density
    for (i in active.indices) {
        for (j in i + 1 until active.size) {
            val a = active[i]
            val b = active[j]
            val dx = b.x - a.x
            val dy = b.y - a.y
            val dist = max(hypot(dx, dy), 0.01f)
            val minDist = a.radius * a.scale.value + b.radius * b.scale.value + gap
            if (dist >= minDist) continue
            val nx = dx / dist
            val ny = dy / dist
            val overlap = minDist - dist
            // La burbuja arrastrada actúa como si tuviera masa infinita
            val (wa, wb) = when {
                a.dragging && b.dragging -> 0f to 0f
                a.dragging -> 0f to 1f
                b.dragging -> 1f to 0f
                else -> 0.5f to 0.5f
            }
            a.x -= nx * overlap * wa
            a.y -= ny * overlap * wa
            b.x += nx * overlap * wb
            b.y += ny * overlap * wb
            // Rebote elástico a lo largo de la normal
            val rel = (b.vx - a.vx) * nx + (b.vy - a.vy) * ny
            if (rel < 0) {
                val impulse = -rel * 0.9f
                a.vx -= nx * impulse * wa * 2
                a.vy -= ny * impulse * wa * 2
                b.vx += nx * impulse * wb * 2
                b.vy += ny * impulse * wb * 2
            }
        }
    }

    // Integración y paredes
    for (b in active) {
        if (b.dragging) continue
        b.x += b.vx * dt
        b.y += b.vy * dt
        val r = b.radius * b.scale.value
        if (b.x < r) { b.x = r; b.vx = kotlin.math.abs(b.vx) * 0.6f }
        if (b.x > w - r) { b.x = w - r; b.vx = -kotlin.math.abs(b.vx) * 0.6f }
        if (b.y < r) { b.y = r; b.vy = kotlin.math.abs(b.vy) * 0.6f }
        if (b.y > h - r) { b.y = h - r; b.vy = -kotlin.math.abs(b.vy) * 0.6f }
    }
}
