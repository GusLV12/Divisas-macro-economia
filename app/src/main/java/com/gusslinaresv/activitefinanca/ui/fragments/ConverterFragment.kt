package com.gusslinaresv.activitefinanca.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.gusslinaresv.activitefinanca.data.CurrencyRepository
import com.gusslinaresv.activitefinanca.ui.components.AnimatedNumber
import com.gusslinaresv.activitefinanca.ui.components.CurrencyPickerSheet
import com.gusslinaresv.activitefinanca.ui.components.CurrencyPill
import com.gusslinaresv.activitefinanca.ui.components.FlagBadge
import com.gusslinaresv.activitefinanca.ui.components.formatAmount
import com.gusslinaresv.activitefinanca.ui.theme.NumberStyle
import com.gusslinaresv.activitefinanca.viewmodel.FinanceViewModel

/** Fragment 3 — Conversor con teclado numérico propio y resultado animado. */
class ConverterFragment : Fragment() {

    private val viewModel: FinanceViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        themedContent { ConverterScreen(viewModel) }
}

private enum class PickerTarget { FROM, TO }

@Composable
private fun ConverterScreen(vm: FinanceViewModel) {
    val from = CurrencyRepository.get(vm.fromCode)
    val to = CurrencyRepository.get(vm.toCode)
    var picker by remember { mutableStateOf<PickerTarget?>(null) }
    var swapTurns by remember { mutableFloatStateOf(0f) }
    val swapRotation by animateFloatAsState(swapTurns * 180f, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "swap")
    val haptics = LocalHapticFeedback.current

    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Box {
                Column(Modifier.padding(20.dp)) {
                    Text("Tengo", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CurrencyPill(from, onClick = { picker = PickerTarget.FROM })
                        Spacer(Modifier.weight(1f))
                        Text(
                            from.symbol + vm.amountText,
                            style = MaterialTheme.typography.headlineMedium.merge(NumberStyle),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 22.dp))
                    Text("Recibo", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CurrencyPill(to, onClick = { picker = PickerTarget.TO })
                        Spacer(Modifier.weight(1f))
                        AnimatedNumber(
                            value = vm.converted(),
                            prefix = to.symbol,
                            style = MaterialTheme.typography.headlineMedium.merge(NumberStyle).copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }
                }
                // Botón de intercambio: gira 180° con rebote en cada toque
                FilledIconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        swapTurns += 1
                        vm.swap()
                    },
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp).size(48.dp)
                ) {
                    Icon(Icons.Filled.SwapVert, contentDescription = "Intercambiar divisas", modifier = Modifier.rotate(swapRotation))
                }
            }
        }

        Text(
            "1 ${from.code} = ${formatAmount(CurrencyRepository.convert(1.0, from, to))} ${to.code}  ·  tasas de ejemplo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp)
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
            items(listOf("1", "10", "100", "500", "1000", "5000")) { q ->
                AssistChip(onClick = { vm.amountText = q }, label = { Text("${from.symbol}$q") })
            }
        }

        Spacer(Modifier.height(12.dp))
        Keypad(onKey = {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            vm.onKey(it)
        })

        Spacer(Modifier.height(20.dp))
        Text("Tu cantidad en otras divisas", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(vm.currencies.filter { it.code != from.code }, key = { it.code }) { c ->
                Card(onClick = { vm.toCode = c.code }, shape = RoundedCornerShape(18.dp)) {
                    Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        FlagBadge(c, size = 30.dp)
                        Text(c.code, style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 4.dp))
                        Text(formatAmount(CurrencyRepository.convert(vm.amount, from, c)), style = MaterialTheme.typography.bodySmall.merge(NumberStyle))
                    }
                }
            }
        }
    }

    picker?.let { target ->
        CurrencyPickerSheet(
            currencies = vm.currencies,
            selectedCode = if (target == PickerTarget.FROM) vm.fromCode else vm.toCode,
            onPick = {
                if (target == PickerTarget.FROM) vm.fromCode = it.code else vm.toCode = it.code
                picker = null
            },
            onDismiss = { picker = null }
        )
    }
}

@Composable
private fun Keypad(onKey: (String) -> Unit) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", "⌫"),
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key -> KeyButton(key, Modifier.weight(1f), onKey) }
            }
        }
        KeyButton("C", Modifier.fillMaxWidth(), onKey, label = "Borrar todo")
    }
}

/** Tecla que se encoge ligeramente al presionarla. */
@Composable
private fun KeyButton(key: String, modifier: Modifier, onKey: (String) -> Unit, label: String = key) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.9f else 1f, label = "key")
    val isAction = key == "⌫" || key == "C"
    Surface(
        onClick = { onKey(key) },
        interactionSource = interaction,
        shape = RoundedCornerShape(18.dp),
        color = if (isAction) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
        modifier = modifier.height(if (key == "C") 48.dp else 58.dp).scale(scale)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = if (key == "C") MaterialTheme.typography.labelLarge else MaterialTheme.typography.headlineSmall)
        }
    }
}
