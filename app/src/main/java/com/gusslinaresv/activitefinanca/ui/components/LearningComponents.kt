package com.gusslinaresv.activitefinanca.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.gusslinaresv.activitefinanca.data.model.Currency
import com.gusslinaresv.activitefinanca.data.model.Milestone
import com.gusslinaresv.activitefinanca.ui.theme.LocalMarketColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Hace aparecer su contenido (deslizándose hacia arriba) la primera vez que entra en pantalla al hacer scroll.
 * El contenido recibe el progreso 0..1 por si quiere animar algo más con él.
 */
@Composable
fun Reveal(modifier: Modifier = Modifier, delayMillis: Int = 0, content: @Composable (progress: Float) -> Unit) {
    var visible by rememberSaveable { mutableStateOf(false) }
    val windowHeight = LocalWindowInfo.current.containerSize.height
    val progress by animateFloatAsState(
        if (visible) 1f else 0f,
        tween(600, delayMillis = delayMillis, easing = FastOutSlowInEasing),
        label = "reveal"
    )
    Box(
        modifier
            .onGloballyPositioned { if (!visible && it.boundsInWindow().top < windowHeight * 0.92f) visible = true }
            .graphicsLayer {
                alpha = progress
                translationY = (1f - progress) * 48.dp.toPx()
            }
    ) { content(progress) }
}

/** Texto que se escribe letra por letra, como una máquina de escribir. */
@Composable
fun TypewriterText(text: String, style: TextStyle, modifier: Modifier = Modifier, charDelayMillis: Long = 35) {
    var shown by remember(text) { mutableIntStateOf(0) }
    LaunchedEffect(text) {
        while (shown < text.length) {
            delay(charDelayMillis)
            shown++
        }
    }
    // Se reserva el espacio del texto completo para que el diseño no "salte" mientras se escribe
    Box(modifier) {
        Text(text, style = style, color = Color.Transparent)
        Text(text.take(shown), style = style)
    }
}

/** Línea del tiempo: cada hito aparece al hacer scroll y la línea se "dibuja" hacia abajo. */
@Composable
fun Timeline(milestones: List<Milestone>, accent: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        milestones.forEachIndexed { i, m ->
            Reveal(delayMillis = 80) { progress ->
                Row(Modifier.height(IntrinsicSize.Min)) {
                    Box(Modifier.width(28.dp).fillMaxHeight()) {
                        Canvas(Modifier.fillMaxSize()) {
                            val x = size.width / 2
                            val dotY = 16.dp.toPx()
                            if (i < milestones.lastIndex) {
                                drawLine(
                                    accent.copy(alpha = 0.4f),
                                    Offset(x, dotY),
                                    Offset(x, dotY + (size.height - dotY) * progress),
                                    strokeWidth = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                            drawCircle(accent, radius = 8.dp.toPx() * progress, center = Offset(x, dotY))
                            drawCircle(Color.White, radius = 3.dp.toPx() * progress, center = Offset(x, dotY))
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.padding(bottom = 22.dp)) {
                        Surface(shape = RoundedCornerShape(50), color = accent.copy(alpha = 0.15f)) {
                            Text(
                                if (m.year < 1000) "c. ${m.year}" else "${m.year}",
                                style = MaterialTheme.typography.labelLarge,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                        Text(m.text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

/**
 * Mazo de tarjetas de curiosidades: se desliza la de arriba hacia un lado (o se toca "Siguiente")
 * y sale volando para revelar la siguiente.
 */
@Composable
fun FactDeck(facts: List<String>, accent: Color, modifier: Modifier = Modifier) {
    var index by rememberSaveable { mutableIntStateOf(0) }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val finished = index >= facts.size

    fun throwCard(direction: Float) {
        scope.launch {
            offsetX.animateTo(direction * 1400f, tween(260))
            index++
            offsetX.snapTo(0f)
        }
    }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.fillMaxWidth().height(190.dp), contentAlignment = Alignment.Center) {
            if (finished) {
                Text("¡Viste todas las curiosidades! 🎉", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            }
            // Se dibujan hasta 3 tarjetas apiladas; la de arriba es la que se arrastra
            for (depth in 2 downTo 0) {
                val i = index + depth
                if (i >= facts.size) continue
                val isTop = depth == 0
                val drag = if (isTop) offsetX.value else 0f
                // Las tarjetas de atrás se acercan conforme la de arriba se va
                val lift = (abs(offsetX.value) / 600f).coerceIn(0f, 1f)
                val effectiveDepth = if (isTop) 0f else depth - lift
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
                    shadowElevation = (6 - depth * 2).dp,
                    modifier = Modifier
                        .zIndex(3f - depth)
                        .fillMaxWidth(0.92f)
                        .height(170.dp)
                        .graphicsLayer {
                            translationX = drag
                            translationY = effectiveDepth * 12.dp.toPx()
                            rotationZ = drag / 40f
                            val s = 1f - effectiveDepth * 0.05f
                            scaleX = s
                            scaleY = s
                        }
                        .then(
                            if (isTop) Modifier.pointerInput(index) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        if (abs(offsetX.value) > 250f) throwCard(if (offsetX.value > 0) 1f else -1f)
                                        else scope.launch { offsetX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }
                                    }
                                ) { change, amount ->
                                    change.consume()
                                    scope.launch { offsetX.snapTo(offsetX.value + amount) }
                                }
                            } else Modifier
                        )
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("💡 Curiosidad ${i + 1} de ${facts.size}", style = MaterialTheme.typography.labelLarge, color = accent)
                        Spacer(Modifier.height(8.dp))
                        Text(facts[i], style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        if (finished) {
            OutlinedButton(onClick = { index = 0 }) { Text("Verlas otra vez ↺") }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(facts.size) { i ->
                    val active = i == index
                    val w by animateFloatAsState(if (active) 22f else 8f, label = "dot")
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .size(width = w.dp, height = 8.dp)
                            .background(if (i <= index) accent else MaterialTheme.colorScheme.outline, RoundedCornerShape(50))
                    )
                }
                Spacer(Modifier.width(16.dp))
                OutlinedButton(onClick = { throwCard(1f) }) { Text("Siguiente →") }
            }
        }
    }
}

/** Pregunta generada a partir de los datos de la divisa. */
private data class LessonQuestion(val text: String, val options: List<String>, val correct: Int)

private fun buildQuestions(currency: Currency, all: List<Currency>): List<LessonQuestion> {
    val random = Random(currency.code.hashCode())
    // 1) ¿Quién la emite?
    val banks = (all.filter { it.code != currency.code }.map { it.centralBank }.shuffled(random).take(3) + currency.centralBank).shuffled(random)
    val q1 = LessonQuestion("¿Qué institución emite el ${currency.name}?", banks, banks.indexOf(currency.centralBank))
    // 2) ¿En qué año ocurrió un hito de su historia?
    val m = currency.milestones.random(random)
    val years = buildSet {
        add(m.year)
        currency.milestones.map { it.year }.filter { it != m.year }.forEach { if (size < 4) add(it) }
        // Si faltan opciones, se agregan años cercanos (±8, ±16, ...)
        var k = 1
        while (size < 4) { add(m.year + if (k % 2 == 0) 8 * k else -8 * k); k++ }
    }.toList().shuffled(random)
    val q2 = LessonQuestion("¿En qué año?: \"${m.text}\"", years.map { it.toString() }, years.indexOf(m.year))
    // 3) ¿Cuántas unidades por dólar?
    val rate = currency.perUsd
    val q3 = if (currency.code == "USD") {
        LessonQuestion("¿Qué porcentaje aproximado de las reservas mundiales está en dólares?", listOf("5 %", "20 %", "58 %", "90 %"), 2)
    } else {
        val opts = listOf(rate, rate * 0.5, rate * 2.2, rate * 1.6).map { formatAmount(it) }.distinct()
        val shuffled = opts.shuffled(random)
        LessonQuestion("Aproximadamente, ¿cuántos ${currency.code} vale 1 dólar?", shuffled, shuffled.indexOf(formatAmount(rate)))
    }
    return listOf(q1, q2, q3)
}

/**
 * Mini-quiz al final de la lección. Si se responden todas bien, se lanza confeti y se llama a [onCompleted].
 */
@Composable
fun LessonQuiz(currency: Currency, all: List<Currency>, alreadyLearned: Boolean, onCompleted: () -> Unit, modifier: Modifier = Modifier) {
    val questions = remember(currency) { buildQuestions(currency, all) }
    var step by remember { mutableIntStateOf(0) }
    var answers by remember { mutableStateOf(listOf<Int>()) }
    // Se captura al inicio: el confeti solo se muestra si la insignia se gana en esta visita
    val wasLearned = remember { alreadyLearned }
    val market = LocalMarketColors.current
    val done = step >= questions.size
    val allCorrect = done && answers.indices.all { answers[it] == questions[it].correct }

    LaunchedEffect(allCorrect) { if (allCorrect) onCompleted() }

    Box(modifier) {
        Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
            AnimatedContent(
                targetState = step,
                transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.9f)) togetherWith fadeOut() },
                label = "lessonQuiz",
                modifier = Modifier.padding(18.dp)
            ) { s ->
                if (s >= questions.size) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        val correct = answers.indices.count { answers[it] == questions[it].correct }
                        Text(if (allCorrect) "🏅" else "📚", style = MaterialTheme.typography.displaySmall)
                        Text(
                            if (allCorrect) "¡Lección completada!" else "$correct de ${questions.size} correctas",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            if (allCorrect) "Ganaste la insignia del ${currency.code}. Aparecerá en su burbuja."
                            else "Repasa la lección y vuelve a intentarlo para ganar la insignia.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        if (!allCorrect) {
                            Spacer(Modifier.height(10.dp))
                            Button(onClick = { step = 0; answers = emptyList() }) { Text("Intentar de nuevo") }
                        }
                    }
                } else {
                    val q = questions[s]
                    val chosen = answers.getOrNull(s)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Pregunta ${s + 1} de ${questions.size}", style = MaterialTheme.typography.labelLarge)
                        Text(q.text, style = MaterialTheme.typography.titleMedium)
                        q.options.forEachIndexed { i, option ->
                            val bg by animateColorAsState(
                                when {
                                    chosen == null -> MaterialTheme.colorScheme.surface
                                    i == q.correct -> market.gain.copy(alpha = 0.25f)
                                    i == chosen -> market.loss.copy(alpha = 0.25f)
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                label = "opt"
                            )
                            Surface(
                                onClick = {
                                    if (chosen != null) return@Surface
                                    answers = answers + i
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = bg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(option, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(14.dp))
                            }
                        }
                        if (chosen != null) {
                            Button(onClick = { step++ }, modifier = Modifier.fillMaxWidth()) {
                                Text(if (s == questions.lastIndex) "Ver resultado" else "Siguiente")
                            }
                        }
                    }
                }
            }
        }
        if (allCorrect && !wasLearned) Confetti(Modifier.matchParentSize())
    }
}

/** Explosión de confeti dibujada con Canvas: partículas con velocidad inicial, gravedad y giro. */
@Composable
fun Confetti(modifier: Modifier = Modifier) {
    val colors = listOf(Color(0xFF10B981), Color(0xFFF6C453), Color(0xFF3B82F6), Color(0xFFEF4444), Color(0xFFA855F7))
    val particles = remember {
        val r = Random(7)
        List(70) {
            val angle = r.nextFloat() * Math.PI.toFloat() * 2
            val speed = 300f + r.nextFloat() * 700f
            Triple(Offset(cos(angle) * speed, sin(angle) * speed - 500f), colors[it % colors.size], r.nextFloat() * 360f)
        }
    }
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) { t.animateTo(1.8f, tween(1800, easing = LinearEasing)) }
    Canvas(modifier) {
        val time = t.value
        if (time >= 1.8f) return@Canvas
        val origin = Offset(size.width / 2, size.height / 2)
        particles.forEach { (v, color, rot) ->
            val pos = origin + Offset(v.x * time, v.y * time + 900f * time * time)
            val alpha = (1f - time / 1.8f).coerceIn(0f, 1f)
            val w = 10f
            val h = 5f + 4f * abs(sin(rot + time * 12))
            drawRect(color.copy(alpha = alpha), topLeft = pos - Offset(w / 2, h / 2), size = androidx.compose.ui.geometry.Size(w, h))
        }
    }
}

/** Barra fina con el progreso de lectura de la pantalla. */
@Composable
fun ReadingProgress(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(fraction, label = "reading")
    Box(modifier.fillMaxWidth().height(4.dp).background(color.copy(alpha = 0.15f))) {
        Box(Modifier.fillMaxWidth(animated).fillMaxHeight().background(color, CircleShape))
    }
}
