package com.gusslinaresv.activitefinanca.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.gusslinaresv.activitefinanca.data.CurrencyRepository
import com.gusslinaresv.activitefinanca.data.UserPreferences
import com.gusslinaresv.activitefinanca.data.model.Currency
import com.gusslinaresv.activitefinanca.ui.components.AnimatedNumber
import com.gusslinaresv.activitefinanca.ui.components.ChangeChip
import com.gusslinaresv.activitefinanca.ui.components.DetailTopBar
import com.gusslinaresv.activitefinanca.ui.components.FactDeck
import com.gusslinaresv.activitefinanca.ui.components.FavoriteButton
import com.gusslinaresv.activitefinanca.ui.components.FlagBadge
import com.gusslinaresv.activitefinanca.ui.components.InteractiveLineChart
import com.gusslinaresv.activitefinanca.ui.components.LessonQuiz
import com.gusslinaresv.activitefinanca.ui.components.ReadingProgress
import com.gusslinaresv.activitefinanca.ui.components.Reveal
import com.gusslinaresv.activitefinanca.ui.components.Timeline
import com.gusslinaresv.activitefinanca.ui.components.TypewriterText
import com.gusslinaresv.activitefinanca.ui.components.formatAmount
import com.gusslinaresv.activitefinanca.ui.theme.ActiviteFinancaTheme
import com.gusslinaresv.activitefinanca.ui.theme.LocalMarketColors
import com.gusslinaresv.activitefinanca.ui.theme.NumberStyle

/**
 * Activity 4 — Lección animada de una divisa: la bandera "aterriza" desde la burbuja elegida,
 * y al hacer scroll aparecen la cotización, la gráfica, la línea del tiempo, las curiosidades y un mini-quiz.
 */
class CurrencyDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val currency = CurrencyRepository.find(intent.getStringExtra(EXTRA_CURRENCY_CODE))
        if (currency == null) {
            finish()
            return
        }
        setContent {
            ActiviteFinancaTheme {
                CurrencyLessonScreen(currency, onBack = ::finish)
            }
        }
    }

    companion object {
        const val EXTRA_CURRENCY_CODE = "extra_currency_code"
    }
}

@Composable
private fun CurrencyLessonScreen(currency: Currency, onBack: () -> Unit) {
    val favorites by UserPreferences.favorites.collectAsState()
    val baseCode by UserPreferences.baseCurrency.collectAsState()
    val learned by UserPreferences.learned.collectAsState()
    val scroll = rememberScrollState()
    val accent = MaterialTheme.colorScheme.primary

    // Si la divisa es la misma que la base, la comparamos contra el dólar (o contra el euro si es el USD)
    val target = CurrencyRepository.get(
        when {
            currency.code != baseCode -> baseCode
            currency.code != "USD" -> "USD"
            else -> "EUR"
        }
    )
    // Valor de 1 unidad de la divisa en la divisa destino, mes a mes
    val series = remember(currency, target) { currency.history.indices.map { i -> target.history[i] / currency.history[i] } }
    val yearChange = (series.last() / series.first() - 1) * 100
    val market = LocalMarketColors.current

    Scaffold(
        topBar = {
            Column {
                DetailTopBar(currency.name, onBack) {
                    FavoriteButton(currency.code in favorites, onToggle = { UserPreferences.toggleFavorite(currency.code) })
                }
                // Cuánto de la lección se ha leído
                ReadingProgress(
                    fraction = if (scroll.maxValue > 0) scroll.value / scroll.maxValue.toFloat() else 0f,
                    color = accent
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scroll)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Hero(currency, isLearned = currency.code in learned)

            // 1. Cotización con contador animado
            Reveal {
                Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(18.dp)) {
                        LessonTitle("💱", "¿Cuánto vale?")
                        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 8.dp)) {
                            Text("1 ${currency.code} = ", style = MaterialTheme.typography.titleLarge)
                            AnimatedNumber(
                                value = series.last(),
                                from = 0.0,
                                style = MaterialTheme.typography.headlineMedium.merge(NumberStyle).copy(color = accent)
                            )
                            Text(" ${target.code}", style = MaterialTheme.typography.titleLarge)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                            Text("En 12 meses ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            ChangeChip(yearChange)
                        }
                        Spacer(Modifier.height(14.dp))
                        InteractiveLineChart(
                            values = series,
                            labels = CurrencyRepository.months,
                            lineColor = if (yearChange >= 0) market.gain else market.loss,
                            valueFormatter = { "${target.symbol}${formatAmount(it)} ${target.code}" }
                        )
                        Text(
                            "👆 Toca o arrastra sobre la gráfica para viajar mes a mes." +
                                if (currency.code != "USD") "  ·  1 USD = ${formatAmount(currency.perUsd)} ${currency.code} (tasas de ejemplo)" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Reveal {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Máximo", formatAmount(series.max()), Modifier.weight(1f))
                    StatCard("Mínimo", formatAmount(series.min()), Modifier.weight(1f))
                    StatCard("Se negocia", "${formatAmount(currency.tradeShare)} %", Modifier.weight(1f))
                }
            }

            // 2. Contexto
            Reveal {
                Column {
                    LessonTitle("📖", "En pocas palabras")
                    Text(currency.summary, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                    Text(currency.story, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 10.dp))
                }
            }

            // 3. Línea del tiempo
            Reveal { LessonTitle("📜", "Su historia en 4 momentos") }
            Timeline(currency.milestones, accent = accent)

            // 4. Banco central con ícono que "late"
            Reveal { CentralBankCard(currency) }

            // 5. Curiosidades en mazo deslizable
            Reveal { LessonTitle("💡", "¿Sabías que…?") }
            Reveal { FactDeck(currency.facts, accent = MaterialTheme.colorScheme.tertiary) }

            // 6. Mini-quiz final
            Reveal { LessonTitle("🧠", "¿Qué aprendiste?") }
            Reveal {
                LessonQuiz(
                    currency = currency,
                    all = CurrencyRepository.currencies,
                    alreadyLearned = currency.code in learned,
                    onCompleted = { UserPreferences.markLearned(currency.code) }
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

/** Encabezado: la bandera llega grande (continuando la burbuja), se asienta con rebote y queda flotando con un anillo que gira. */
@Composable
private fun Hero(currency: Currency, isLearned: Boolean) {
    val landing = remember { Animatable(2.6f) }
    LaunchedEffect(Unit) { landing.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
    val infinite = rememberInfiniteTransition(label = "hero")
    val float by infinite.animateFloat(-5f, 5f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "float")
    val ring by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(9000, easing = LinearEasing)), label = "ring")
    val ringColor = if (isLearned) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(150.dp).graphicsLayer {
                scaleX = landing.value
                scaleY = landing.value
                translationY = float * density
            }
        ) {
            Canvas(Modifier.fillMaxSize().graphicsLayer { rotationZ = ring }) {
                drawCircle(
                    ringColor,
                    style = Stroke(width = 3.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 14f)))
                )
            }
            FlagBadge(currency, size = 120.dp)
        }
        Spacer(Modifier.height(14.dp))
        TypewriterText(currency.name, style = MaterialTheme.typography.headlineMedium)
        Text(
            "${currency.code} · ${currency.symbol} · ${currency.country}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 6.dp)) {
            AssistChip(onClick = {}, label = { Text("\"${currency.nickname}\"") })
            AssistChip(onClick = {}, label = { Text(currency.region.label) })
            if (isLearned) AssistChip(onClick = {}, label = { Text("🏅 Aprendida") })
        }
    }
}

@Composable
private fun LessonTitle(emoji: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun CentralBankCard(currency: Currency) {
    val pulse by rememberInfiniteTransition(label = "bank").animateFloat(
        1f, 1.12f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pulse"
    )
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(56.dp).graphicsLayer { scaleX = pulse; scaleY = pulse }) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
                }
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("🏦 ¿Quién la emite?", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                Text(currency.centralBank, style = MaterialTheme.typography.titleMedium)
                Text(
                    "Emite los billetes y decide la política monetaria: por ejemplo, sube o baja la tasa de interés para controlar la inflación.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Card(shape = RoundedCornerShape(18.dp), modifier = modifier) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.titleSmall.merge(NumberStyle), maxLines = 1)
        }
    }
}
