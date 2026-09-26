package com.gusslinaresv.activitefinanca.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.gusslinaresv.activitefinanca.data.CurrencyRepository
import com.gusslinaresv.activitefinanca.data.UserPreferences
import com.gusslinaresv.activitefinanca.data.model.Currency
import com.gusslinaresv.activitefinanca.ui.components.Bullet
import com.gusslinaresv.activitefinanca.ui.components.ChangeChip
import com.gusslinaresv.activitefinanca.ui.components.DetailTopBar
import com.gusslinaresv.activitefinanca.ui.components.ExpandableSection
import com.gusslinaresv.activitefinanca.ui.components.FavoriteButton
import com.gusslinaresv.activitefinanca.ui.components.FlagBadge
import com.gusslinaresv.activitefinanca.ui.components.InteractiveLineChart
import com.gusslinaresv.activitefinanca.ui.components.formatAmount
import com.gusslinaresv.activitefinanca.ui.components.formatPercent
import com.gusslinaresv.activitefinanca.ui.theme.ActiviteFinancaTheme
import com.gusslinaresv.activitefinanca.ui.theme.LocalMarketColors
import com.gusslinaresv.activitefinanca.ui.theme.NumberStyle

/** Activity 4 — Detalle de una divisa: cotización, gráfica interactiva, historia y curiosidades. */
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
                CurrencyDetailScreen(currency, onBack = ::finish)
            }
        }
    }

    companion object {
        const val EXTRA_CURRENCY_CODE = "extra_currency_code"
    }
}

@Composable
private fun CurrencyDetailScreen(currency: Currency, onBack: () -> Unit) {
    val favorites by UserPreferences.favorites.collectAsState()
    val baseCode by UserPreferences.baseCurrency.collectAsState()
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

    val flagScale = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) { flagScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy)) }

    Scaffold(
        topBar = {
            DetailTopBar(currency.name, onBack) {
                FavoriteButton(currency.code in favorites, onToggle = { UserPreferences.toggleFavorite(currency.code) })
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FlagBadge(currency, size = 76.dp, modifier = Modifier.graphicsLayer { scaleX = flagScale.value; scaleY = flagScale.value })
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("${currency.code} · ${currency.symbol}", style = MaterialTheme.typography.headlineMedium)
                    Text(currency.country, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {}, label = { Text("\"${currency.nickname}\"") })
                        AssistChip(onClick = {}, label = { Text(currency.region.label) })
                    }
                }
            }

            Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "1 ${currency.code} en ${target.code} · últimos 12 meses",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        ChangeChip(yearChange)
                    }
                    Spacer(Modifier.height(8.dp))
                    InteractiveLineChart(
                        values = series,
                        labels = CurrencyRepository.months,
                        lineColor = if (yearChange >= 0) market.gain else market.loss,
                        valueFormatter = { "${target.symbol}${formatAmount(it)} ${target.code}" }
                    )
                    Text(
                        "Toca o arrastra sobre la gráfica para ver cada mes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Máximo", formatAmount(series.max()), Modifier.weight(1f))
                StatCard("Mínimo", formatAmount(series.min()), Modifier.weight(1f))
                StatCard("12 meses", formatPercent(yearChange), Modifier.weight(1f))
            }

            if (currency.code != "USD") Text(
                "1 USD = ${formatAmount(currency.perUsd)} ${currency.code}  ·  tasas de ejemplo",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ExpandableSection("Resumen", initiallyExpanded = true) {
                Text(currency.summary, style = MaterialTheme.typography.bodyLarge)
            }
            ExpandableSection("Historia") {
                Text(currency.story, style = MaterialTheme.typography.bodyLarge)
            }
            ExpandableSection("Banco central") {
                Column {
                    Text(currency.centralBank, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Emite la moneda y decide la política monetaria. En uso desde ${currency.since}.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            ExpandableSection("Curiosidades", initiallyExpanded = true) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    currency.facts.forEach { Bullet(it) }
                }
            }
            Spacer(Modifier.height(24.dp))
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
