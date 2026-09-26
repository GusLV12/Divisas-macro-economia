package com.gusslinaresv.activitefinanca.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.gusslinaresv.activitefinanca.data.Banknote
import com.gusslinaresv.activitefinanca.data.BanknoteRepository
import com.gusslinaresv.activitefinanca.data.Hotspot
import com.gusslinaresv.activitefinanca.data.model.Currency
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Dibuja un billete estilizado: fondo con guilloché (líneas onduladas de seguridad), retrato,
 * marca de agua o ventana de polímero, franja holográfica y microimpresión en el borde.
 * [shine] (0..1) desplaza el arcoíris del holograma según la inclinación.
 */
fun DrawScope.drawBanknote(note: Banknote, currency: Currency, back: Boolean, tm: TextMeasurer, shine: Float) {
    val w = size.width
    val h = size.height
    val corner = CornerRadius(h * 0.06f)
    val light = lerp(note.color, Color.White, 0.35f)
    val dark = lerp(note.color, Color.Black, 0.35f)
    val white = Color.White

    drawRoundRect(Brush.linearGradient(listOf(light, note.color, dark), Offset.Zero, Offset(w, h)), cornerRadius = corner)

    // Guilloché: ondas finas que se cruzan, típicas de los billetes
    for (k in 0 until 18) {
        val path = Path()
        val baseY = h * (0.06f + k * 0.052f)
        var x = 0f
        while (x <= w) {
            val y = baseY + sin(x / w * 2 * PI.toFloat() * (2 + k % 3) + k) * h * 0.03f
            if (x == 0f) path.moveTo(x, y) else path.lineTo(x, y)
            x += w / 60f
        }
        drawPath(path, white.copy(alpha = 0.09f), style = Stroke(width = 1.dp.toPx()))
    }

    // Marco
    val inset = h * 0.05f
    drawRoundRect(
        white.copy(alpha = 0.5f), topLeft = Offset(inset, inset), size = Size(w - inset * 2, h - inset * 2),
        cornerRadius = CornerRadius(h * 0.04f), style = Stroke(width = 1.5.dp.toPx())
    )

    // Microimpresión: texto diminuto en el borde inferior (solo legible con la lupa)
    drawText(
        tm, (currency.centralBank.uppercase() + " · ").repeat(14),
        topLeft = Offset(inset * 1.4f, h - inset * 1.85f),
        style = TextStyle(color = white.copy(alpha = 0.75f), fontSize = (h * 0.026f).toSp(), fontWeight = FontWeight.Bold),
        softWrap = false, maxLines = 1, overflow = TextOverflow.Clip,
        size = Size(w - inset * 2.8f, h * 0.05f)
    )

    val denomination = "${currency.symbol}${formatAmount(note.denomination.toDouble()).substringBefore('.')}"

    if (!back) {
        // Valor grande
        drawText(tm, denomination, topLeft = Offset(inset * 1.8f, inset * 1.4f),
            style = TextStyle(color = white, fontSize = (h * 0.15f).toSp(), fontWeight = FontWeight.Black))
        drawText(tm, currency.name.uppercase(), topLeft = Offset(inset * 1.9f, h * 0.29f),
            style = TextStyle(color = white.copy(alpha = 0.85f), fontSize = (h * 0.045f).toSp(), fontWeight = FontWeight.Bold))

        // Retrato en óvalo con silueta
        val cx = w * 0.72f
        val cy = h * 0.48f
        val rx = w * 0.13f
        val ry = h * 0.33f
        drawOval(white.copy(alpha = 0.18f), topLeft = Offset(cx - rx, cy - ry), size = Size(rx * 2, ry * 2))
        drawOval(white.copy(alpha = 0.55f), topLeft = Offset(cx - rx, cy - ry), size = Size(rx * 2, ry * 2), style = Stroke(1.5.dp.toPx()))
        val silhouette = dark.copy(alpha = 0.75f)
        drawCircle(silhouette, radius = rx * 0.36f, center = Offset(cx, cy - ry * 0.22f))
        val shoulders = Path().apply {
            moveTo(cx - rx * 0.75f, cy + ry * 0.78f)
            quadraticTo(cx - rx * 0.7f, cy + ry * 0.2f, cx, cy + ry * 0.18f)
            quadraticTo(cx + rx * 0.7f, cy + ry * 0.2f, cx + rx * 0.75f, cy + ry * 0.78f)
            close()
        }
        drawPath(shoulders, silhouette)
        drawText(tm, note.portrait, topLeft = Offset(cx - rx * 1.4f, cy + ry + h * 0.005f),
            style = TextStyle(color = white, fontSize = (h * 0.04f).toSp(), textAlign = TextAlign.Center),
            maxLines = 1, overflow = TextOverflow.Ellipsis, size = Size(rx * 2.8f, h * 0.06f))

        // Marca de agua (papel) o ventana transparente (polímero)
        val wc = Offset(w * 0.2f, h * 0.58f)
        val wr = h * 0.17f
        if (note.polymer) {
            drawCircle(white.copy(alpha = 0.55f), radius = wr, center = wc)
            drawCircle(white, radius = wr, center = wc, style = Stroke(1.5.dp.toPx()))
        } else {
            drawCircle(white.copy(alpha = 0.10f), radius = wr, center = wc)
        }
        drawText(tm, currency.symbol, topLeft = Offset(wc.x - wr * 0.5f, wc.y - wr * 0.75f),
            style = TextStyle(color = (if (note.polymer) dark else white).copy(alpha = 0.35f), fontSize = (wr * 1.1f).toSp(), fontWeight = FontWeight.Black))

        // Franja holográfica: el arcoíris se desplaza al inclinar el billete
        val stripX = w * 0.4f
        val stripW = w * 0.05f
        val rainbow = listOf(Color(0xFFFF6B6B), Color(0xFFFFD93D), Color(0xFF6BCB77), Color(0xFF4D96FF), Color(0xFFB980F0), Color(0xFFFF6B6B))
        drawRect(
            Brush.linearGradient(rainbow.map { it.copy(alpha = 0.6f) }, Offset(0f, h * (shine - 1f)), Offset(0f, h * (shine + 1f))),
            topLeft = Offset(stripX, inset), size = Size(stripW, h - inset * 2)
        )
    } else {
        // Reverso: símbolo gigante de fondo, motivo y valor
        drawText(tm, currency.symbol, topLeft = Offset(w * 0.3f, -h * 0.12f),
            style = TextStyle(color = white.copy(alpha = 0.12f), fontSize = (h * 0.9f).toSp(), fontWeight = FontWeight.Black))
        drawText(tm, note.reverse, topLeft = Offset(w * 0.12f, h * 0.3f),
            style = TextStyle(color = white, fontSize = (h * 0.07f).toSp(), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
            size = Size(w * 0.76f, h * 0.4f))
        drawText(tm, denomination, topLeft = Offset(w * 0.62f, h * 0.66f),
            style = TextStyle(color = white, fontSize = (h * 0.12f).toSp(), fontWeight = FontWeight.Black, textAlign = TextAlign.End),
            size = Size(w * 0.3f, h * 0.2f))
        drawText(tm, currency.country.uppercase(), topLeft = Offset(inset * 1.9f, inset * 1.6f),
            style = TextStyle(color = white.copy(alpha = 0.85f), fontSize = (h * 0.05f).toSp(), fontWeight = FontWeight.Bold))
    }
}

/**
 * Visor del billete: aparece flotando, se gira con el dedo (y se voltea para ver el reverso),
 * tiene puntos de seguridad tocables y un modo lupa para descubrir la microimpresión.
 */
@Composable
fun BanknoteViewer(
    currency: Currency,
    baseCode: String,
    onClose: () -> Unit,
    onLesson: () -> Unit,
    onConvert: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val note = remember(currency) { BanknoteRepository.get(currency.code) }
    val hotspots = remember(currency, baseCode) { BanknoteRepository.hotspots(note, currency, baseCode) }
    val tm = rememberTextMeasurer()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val rotY = remember { Animatable(0f) }
    val rotX = remember { Animatable(0f) }
    val entrance = remember { Animatable(0f) }
    var lupa by remember { mutableStateOf(false) }
    var lens by remember { mutableStateOf<Offset?>(null) }
    var selected by remember { mutableStateOf<Hotspot?>(null) }

    LaunchedEffect(Unit) {
        entrance.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        // Pequeño giro de presentación para invitar a tocarlo
        rotY.animateTo(-25f, tween(500))
        rotY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
    }

    val normalized = ((rotY.value % 360f) + 360f) % 360f
    val showBack = normalized > 90f && normalized < 270f
    val shine = (((rotY.value + rotX.value * 2f) / 120f) % 1f + 1f) % 1f

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            FlagBadge(currency, size = 36.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("Billete de ${formatAmount(note.denomination.toDouble()).substringBefore('.')} ${currency.code}", style = MaterialTheme.typography.titleLarge)
                Text(currency.name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Cerrar la caja") }
        }
        Spacer(Modifier.height(20.dp))

        // Los gestos se detectan en este contenedor fijo (no en el billete que gira) para que las
        // coordenadas del dedo no se deformen con la rotación 3D
        BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .aspectRatio(2.1f)
                .graphicsLayer {
                    val e = entrance.value
                    scaleX = 0.3f + 0.7f * e
                    scaleY = 0.3f + 0.7f * e
                    alpha = e.coerceIn(0f, 1f)
                    translationY = (1f - e) * 200.dp.toPx()
                    rotationZ = (1f - e) * -12f
                }
                .pointerInput(lupa) {
                    if (lupa) {
                        detectDragGestures(onDragStart = { lens = it }) { change, _ -> lens = change.position }
                    } else {
                        detectDragGestures(
                            onDragEnd = {
                                scope.launch { rotX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                                // Se asienta mostrando la cara más cercana
                                scope.launch {
                                    val target = (rotY.value / 180f).roundToInt() * 180f
                                    rotY.animateTo(target, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                }
                            }
                        ) { change, drag ->
                            change.consume()
                            scope.launch { rotY.snapTo(rotY.value + drag.x * 0.28f) }
                            scope.launch { rotX.snapTo((rotX.value - drag.y * 0.3f).coerceIn(-28f, 28f)) }
                        }
                    }
                }
                .pointerInput(lupa) { if (lupa) detectTapGestures { lens = it } }
        ) {
            val noteW = constraints.maxWidth.toFloat()
            val noteH = constraints.maxHeight.toFloat()
            // El billete que se gira
            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationY = rotY.value
                        rotationX = rotX.value
                        cameraDistance = 14 * density.density
                    }
            ) {
                // La cara trasera se voltea de nuevo para que no se vea en espejo
                Box(Modifier.fillMaxSize().graphicsLayer { if (showBack) rotationY = 180f }) {
                    Canvas(Modifier.fillMaxSize()) { drawBanknote(note, currency, showBack, tm, shine) }
                    if (!lupa) {
                        hotspots.filter { it.back == showBack }.forEach { spot ->
                            HotspotDot(
                                spot = spot,
                                isSelected = selected == spot,
                                modifier = Modifier.offset {
                                    IntOffset((spot.x * noteW - 16.dp.toPx()).roundToInt(), (spot.y * noteH - 16.dp.toPx()).roundToInt())
                                },
                                onClick = { selected = spot }
                            )
                        }
                    }
                }
            }
            // Lupa: dibuja el billete ampliado dentro de un círculo que sigue al dedo
            if (lupa) {
                Canvas(Modifier.fillMaxSize().clipToBounds()) {
                    val p = lens ?: Offset(size.width * 0.5f, size.height * 0.9f)
                    val r = 64.dp.toPx()
                    val circle = Path().apply { addOval(Rect(center = p, radius = r)) }
                    clipPath(circle) {
                        withTransform({ scale(2.8f, 2.8f, pivot = p) }) { drawBanknote(note, currency, showBack, tm, shine) }
                    }
                    drawCircle(Color.White, radius = r, center = p, style = Stroke(4.dp.toPx()))
                    drawCircle(Color.Black.copy(alpha = 0.3f), radius = r + 2.dp.toPx(), center = p, style = Stroke(1.dp.toPx()))
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            if (lupa) "Arrastra la lupa por el billete. Busca el texto diminuto del borde inferior 👀"
            else "Gira el billete con el dedo · voltéalo para ver el reverso · toca los puntos ✨",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !lupa, onClick = { lupa = false }, label = { Text("🔄 Girar") })
            FilterChip(selected = lupa, onClick = {
                lupa = true
                selected = null
                // En modo lupa el billete queda plano
                scope.launch { rotX.animateTo(0f) }
                scope.launch { rotY.animateTo(if (showBack) 180f else 0f) }
            }, label = { Text("🔍 Lupa") })
            FilterChip(selected = false, onClick = {
                scope.launch { rotY.animateTo(rotY.value + 180f, tween(600)) }
            }, label = { Text("↻ Voltear") })
        }

        Spacer(Modifier.height(12.dp))
        AnimatedContent(
            targetState = selected,
            transitionSpec = { (fadeIn() + slideInVertically { it / 3 }) togetherWith fadeOut() },
            label = "hotspotInfo"
        ) { spot ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (spot == null) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    if (spot == null) {
                        Text("✨ Toca un punto brillante del billete para descubrir sus secretos.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text("${spot.icon} ${spot.title}", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(spot.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = onLesson, modifier = Modifier.weight(1f)) { Text("📖 Ver lección") }
            OutlinedButton(onClick = onConvert, modifier = Modifier.weight(1f)) { Text("💱 Convertir") }
        }
    }
}

/** Punto brillante que late sobre el billete. */
@Composable
private fun HotspotDot(spot: Hotspot, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "spot").animateFloat(
        0.85f, 1.15f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulse"
    )
    Box(
        modifier
            .size(32.dp)
            .graphicsLayer {
                val s = if (isSelected) 1.2f else pulse
                scaleX = s
                scaleY = s
            }
            .background(if (isSelected) Color(0xFFF6C453) else Color.White.copy(alpha = 0.85f), CircleShape)
            .border(2.dp, Color.White, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(spot.icon, style = MaterialTheme.typography.labelLarge)
    }
}
