package com.gusslinaresv.activitefinanca.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gusslinaresv.activitefinanca.data.model.SimulatorType
import com.gusslinaresv.activitefinanca.ui.theme.LocalMarketColors
import com.gusslinaresv.activitefinanca.ui.theme.NumberStyle
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.roundToInt

/** Simulador interactivo según el concepto. Devuelve nada si el concepto no tiene simulador. */
@Composable
fun ConceptSimulator(type: SimulatorType, modifier: Modifier = Modifier) {
    when (type) {
        SimulatorType.INFLATION -> InflationSimulator(modifier)
        SimulatorType.COMPOUND_INTEREST -> CompoundInterestSimulator(modifier)
        SimulatorType.EXCHANGE_RATE -> ExchangeRateSimulator(modifier)
        SimulatorType.GDP_GROWTH -> GdpSimulator(modifier)
        SimulatorType.NONE -> Unit
    }
}

@Composable
private fun SimulatorCard(title: String, modifier: Modifier, content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("🎛️ $title", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            content()
        }
    }
}

@Composable
private fun LabeledSlider(label: String, valueText: String, value: Float, range: ClosedFloatingPointRange<Float>, steps: Int = 0, onChange: (Float) -> Unit) {
    Column {
        Row {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(valueText, style = MaterialTheme.typography.titleSmall.merge(NumberStyle))
        }
        Slider(value = value, onValueChange = onChange, valueRange = range, steps = steps)
    }
}

/** Dos barras animadas que comparan "antes" y "después". */
@Composable
private fun CompareBars(leftLabel: String, left: Double, rightLabel: String, right: Double, rightColor: Color) {
    val max = maxOf(left, right).coerceAtLeast(0.0001)
    val l by animateFloatAsState((left / max).toFloat(), label = "left")
    val r by animateFloatAsState((right / max).toFloat(), label = "right")
    val leftColor = MaterialTheme.colorScheme.secondary
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        listOf(Triple(leftLabel, l, leftColor), Triple(rightLabel, r, rightColor)).forEach { (label, fraction, color) ->
            Column(Modifier.weight(1f)) {
                Canvas(Modifier.fillMaxWidth().height(110.dp)) {
                    val h = size.height * fraction
                    drawRoundRect(color.copy(alpha = 0.15f), cornerRadius = CornerRadius(16f))
                    drawRoundRect(color, topLeft = Offset(0f, size.height - h), size = Size(size.width, h), cornerRadius = CornerRadius(16f))
                }
                Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun InflationSimulator(modifier: Modifier) {
    var rate by rememberSaveable { mutableFloatStateOf(5f) }
    var years by rememberSaveable { mutableFloatStateOf(10f) }
    val factor = (1 + rate / 100.0).pow(years.roundToInt())
    val power = 1000 / factor
    SimulatorCard("¿Cuánto valdrán $1,000 en el futuro?", modifier) {
        LabeledSlider("Inflación anual", "${rate.roundToInt()} %", rate, 0f..20f, 19) { rate = it }
        LabeledSlider("Años", "${years.roundToInt()}", years, 1f..30f, 28) { years = it }
        Text(
            "Con ${rate.roundToInt()} % de inflación durante ${years.roundToInt()} años, $1,000 de hoy comprarán lo mismo que $${formatAmount(power)} hoy. " +
                "Algo que cuesta $100 costará $${formatAmount(100 * factor)}.",
            style = MaterialTheme.typography.bodyMedium
        )
        CompareBars("Hoy: $1,000", 1000.0, "Poder de compra: $${power.roundToInt()}", power, LocalMarketColors.current.loss)
    }
}

@Composable
private fun CompoundInterestSimulator(modifier: Modifier) {
    var principal by rememberSaveable { mutableFloatStateOf(1000f) }
    var rate by rememberSaveable { mutableFloatStateOf(10f) }
    var years by rememberSaveable { mutableFloatStateOf(10f) }
    val final = principal * (1 + rate / 100.0).pow(years.roundToInt())
    SimulatorCard("Interés compuesto", modifier) {
        LabeledSlider("Inversión inicial", "$${formatAmount(principal.toDouble())}", principal, 100f..10000f, 98) { principal = (it / 100).roundToInt() * 100f }
        LabeledSlider("Tasa anual", "${rate.roundToInt()} %", rate, 1f..20f, 18) { rate = it }
        LabeledSlider("Años", "${years.roundToInt()}", years, 1f..40f, 38) { years = it }
        Text(
            "Tu dinero crecería a $${formatAmount(final)}. Según la regla del 72, se duplica cada ~${(72 / rate).roundToInt()} años.",
            style = MaterialTheme.typography.bodyMedium
        )
        CompareBars("Inicial", principal.toDouble(), "Final: $${final.roundToInt()}", final, LocalMarketColors.current.gain)
    }
}

@Composable
private fun ExchangeRateSimulator(modifier: Modifier) {
    val reference = 18.4f
    var rate by rememberSaveable { mutableFloatStateOf(reference) }
    val appreciated = rate < reference
    SimulatorCard("Mueve el tipo de cambio USD/MXN", modifier) {
        LabeledSlider("Pesos por dólar", formatAmount(rate.toDouble()), rate, 14f..26f) { rate = it }
        Text(
            when {
                kotlin.math.abs(rate - reference) < 0.05f -> "Estás en el valor de referencia (${formatAmount(reference.toDouble())})."
                appreciated -> "El peso se APRECIÓ: necesitas menos pesos por cada dólar. Viajar e importar es más barato."
                else -> "El peso se DEPRECIÓ: necesitas más pesos por cada dólar. Las exportaciones mexicanas se vuelven más competitivas."
            },
            style = MaterialTheme.typography.bodyMedium
        )
        Text("Un producto de 100 USD cuesta ${formatAmount(100.0 * rate)} MXN", style = MaterialTheme.typography.titleSmall.merge(NumberStyle))
        Text("Con 1,000 MXN compras ${formatAmount(1000.0 / rate)} USD", style = MaterialTheme.typography.titleSmall.merge(NumberStyle))
        CompareBars(
            "Referencia", 100.0 * reference, "Ahora", 100.0 * rate,
            if (appreciated) LocalMarketColors.current.gain else LocalMarketColors.current.loss
        )
    }
}

@Composable
private fun GdpSimulator(modifier: Modifier) {
    var growth by rememberSaveable { mutableFloatStateOf(3f) }
    var years by rememberSaveable { mutableFloatStateOf(20f) }
    val size = (1 + growth / 100.0).pow(years.roundToInt())
    val doubling = if (growth > 0) ln(2.0) / ln(1 + growth / 100.0) else Double.POSITIVE_INFINITY
    SimulatorCard("Crecimiento económico", modifier) {
        LabeledSlider("Crecimiento anual del PIB", "${formatAmount(growth.toDouble())} %", growth, 0f..10f, 19) { growth = it }
        LabeledSlider("Años", "${years.roundToInt()}", years, 1f..50f, 48) { years = it }
        Text(
            "La economía sería ${formatAmount(size)} veces más grande. " +
                if (doubling.isFinite()) "Se duplica cada ${doubling.roundToInt()} años." else "Sin crecimiento no se duplica nunca.",
            style = MaterialTheme.typography.bodyMedium
        )
        CompareBars("PIB hoy", 1.0, "PIB en ${years.roundToInt()} años", size, LocalMarketColors.current.gain)
    }
}
