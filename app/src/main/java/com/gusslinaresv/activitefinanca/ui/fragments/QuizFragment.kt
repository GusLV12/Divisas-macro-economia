package com.gusslinaresv.activitefinanca.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gusslinaresv.activitefinanca.data.UserPreferences
import com.gusslinaresv.activitefinanca.data.model.QuizQuestion
import com.gusslinaresv.activitefinanca.ui.theme.LocalMarketColors
import com.gusslinaresv.activitefinanca.viewmodel.QuizViewModel

/** Fragment 5 — Trivia de divisas y macroeconomía con retroalimentación animada. */
class QuizFragment : Fragment() {

    private val viewModel: QuizViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        themedContent { QuizScreen(viewModel) }
}

@Composable
private fun QuizScreen(vm: QuizViewModel) {
    AnimatedContent(targetState = vm.finished, label = "quizState") { finished ->
        if (finished) ResultScreen(vm) else QuestionScreen(vm)
    }
}

@Composable
private fun QuestionScreen(vm: QuizViewModel) {
    val record by UserPreferences.quizRecord.collectAsState()
    val progress by animateFloatAsState((vm.index + if (vm.selected != null) 1 else 0) / vm.total.toFloat(), label = "progress")

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Pregunta ${vm.index + 1} de ${vm.total}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text("✔ ${vm.score}   🏆 $record", style = MaterialTheme.typography.titleMedium)
        }
        LinearProgressIndicator(
            progress = { progress },
            strokeCap = StrokeCap.Round,
            modifier = Modifier.fillMaxWidth().height(10.dp).padding(top = 6.dp)
        )
        Spacer(Modifier.height(20.dp))

        // Cada pregunta nueva entra deslizándose desde la derecha
        AnimatedContent(
            targetState = vm.index,
            transitionSpec = { (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut()) },
            label = "question"
        ) { index ->
            val question = vm.questions[index]
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text(question.question, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(22.dp))
                }
                question.options.forEachIndexed { i, option ->
                    OptionCard(option, i, question, vm.selected) { vm.answer(i) }
                }
                AnimatedVisibility(vm.selected != null, enter = fadeIn() + slideInVertically { it / 2 }) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        val correct = vm.selected == question.correctIndex
                        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                            Column(Modifier.padding(16.dp)) {
                                Text(if (correct) "¡Correcto! 🎉" else "Casi… 💡", style = MaterialTheme.typography.titleMedium)
                                Text(question.explanation, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Button(onClick = vm::next, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                            Text(if (vm.index == vm.total - 1) "Ver resultado" else "Siguiente")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionCard(text: String, index: Int, question: QuizQuestion, selected: Int?, onClick: () -> Unit) {
    val market = LocalMarketColors.current
    val answered = selected != null
    val isCorrect = index == question.correctIndex
    val isChosen = index == selected
    val targetColor = when {
        answered && isCorrect -> market.gain.copy(alpha = 0.18f)
        answered && isChosen -> market.loss.copy(alpha = 0.18f)
        else -> MaterialTheme.colorScheme.surface
    }
    val color by animateColorAsState(targetColor, label = "optionColor")
    val borderColor = when {
        answered && isCorrect -> market.gain
        answered && isChosen -> market.loss
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    // Sacudida horizontal cuando se elige una respuesta incorrecta
    val shake = remember { Animatable(0f) }
    LaunchedEffect(selected) {
        if (isChosen && !isCorrect) {
            shake.animateTo(0f, keyframes {
                durationMillis = 400
                -18f at 50; 18f at 120; -12f at 190; 12f at 260; -5f at 330
            })
        }
    }

    Surface(
        onClick = onClick,
        enabled = !answered,
        shape = RoundedCornerShape(18.dp),
        color = color,
        border = BorderStroke(if (answered && (isCorrect || isChosen)) 2.dp else 1.dp, borderColor),
        modifier = Modifier.fillMaxWidth().graphicsLayer { translationX = shake.value }
    ) {
        Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${'A' + index}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(14.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (answered && isCorrect) Icon(Icons.Filled.CheckCircle, contentDescription = "Correcta", tint = market.gain)
            if (answered && isChosen && !isCorrect) Icon(Icons.Filled.Cancel, contentDescription = "Incorrecta", tint = market.loss)
        }
    }
}

@Composable
private fun ResultScreen(vm: QuizViewModel) {
    val ratio = vm.score / vm.total.toFloat()
    val arc = remember { Animatable(0f) }
    LaunchedEffect(Unit) { arc.animateTo(ratio, tween(1200)) }
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f, targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "pulseScale"
    )
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    val message = when {
        ratio >= 0.9f -> "¡Eres un experto en finanzas! 🏦"
        ratio >= 0.6f -> "¡Muy bien! Vas por buen camino 📈"
        ratio >= 0.3f -> "Nada mal, repasa la sección Aprende 📚"
        else -> "Cada experto empezó desde cero 💪"
    }

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
            Canvas(Modifier.fillMaxSize()) {
                val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                drawArc(track, -90f, 360f, false, style = stroke)
                drawArc(primary, -90f, 360f * arc.value, false, style = stroke)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${(arc.value * vm.total).toInt()}/${vm.total}", style = MaterialTheme.typography.displaySmall)
                Text("aciertos", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Spacer(Modifier.height(24.dp))
        if (vm.isNewRecord) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.scale(pulse)
            ) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.width(8.dp))
                    Text("¡Nuevo récord!", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        Text(message, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(28.dp))
        Button(onClick = vm::restart, modifier = Modifier.fillMaxWidth().height(52.dp)) {
            Icon(Icons.Filled.Replay, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Jugar otra vez")
        }
    }
}
