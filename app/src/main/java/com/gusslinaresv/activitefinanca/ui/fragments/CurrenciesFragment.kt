package com.gusslinaresv.activitefinanca.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.gusslinaresv.activitefinanca.data.UserPreferences
import com.gusslinaresv.activitefinanca.data.model.Region
import com.gusslinaresv.activitefinanca.ui.components.CurrencyBubbleField
import com.gusslinaresv.activitefinanca.ui.components.CurrencyRow
import com.gusslinaresv.activitefinanca.ui.components.StaggeredAppear
import com.gusslinaresv.activitefinanca.ui.components.SwipeAction
import com.gusslinaresv.activitefinanca.ui.components.formatAmount
import com.gusslinaresv.activitefinanca.ui.openCurrency
import com.gusslinaresv.activitefinanca.viewmodel.FinanceViewModel

/** Fragment 2 — Divisas como burbujas flotantes (o lista), con búsqueda y filtros por región. */
class CurrenciesFragment : Fragment() {

    private val viewModel: FinanceViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        themedContent {
            CurrenciesScreen(viewModel) { code, fromBubble -> requireContext().openCurrency(code, fade = fromBubble) }
        }
}

@Composable
private fun CurrenciesScreen(viewModel: FinanceViewModel, onOpen: (code: String, fromBubble: Boolean) -> Unit) {
    val favorites by viewModel.favorites.collectAsState()
    val base by viewModel.baseCurrency.collectAsState()
    val learned by UserPreferences.learned.collectAsState()
    val list = viewModel.filtered()

    Column {
        OutlinedTextField(
            value = viewModel.query,
            onValueChange = { viewModel.query = it },
            placeholder = { Text("Buscar: yen, euro, México…") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (viewModel.query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.query = "" }) { Icon(Icons.Filled.Clear, contentDescription = "Limpiar") }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Cambiar entre burbujas flotantes y lista clásica
            item {
                FilterChip(
                    selected = viewModel.bubbleMode,
                    onClick = { viewModel.bubbleMode = !viewModel.bubbleMode },
                    label = { Text(if (viewModel.bubbleMode) "Burbujas" else "Lista") },
                    leadingIcon = { Icon(if (viewModel.bubbleMode) Icons.Filled.BubbleChart else Icons.AutoMirrored.Filled.ViewList, contentDescription = null) }
                )
            }
            item {
                FilterChip(selected = viewModel.region == null, onClick = { viewModel.region = null }, label = { Text("Todas") })
            }
            items(Region.entries) { r ->
                FilterChip(
                    selected = viewModel.region == r,
                    onClick = { viewModel.region = if (viewModel.region == r) null else r },
                    label = { Text(r.label) }
                )
            }
        }

        if (viewModel.bubbleMode) {
            Text(
                "El tamaño de cada burbuja es cuánto se negocia esa moneda en el mundo. Arrástralas, lánzalas y toca una para aprender sobre ella. 🏅 = lección completada (${learned.size}/${viewModel.currencies.size})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
            CurrencyBubbleField(
                currencies = viewModel.currencies,
                visibleCodes = list.map { it.code }.toSet(),
                learned = learned,
                onSelect = { onOpen(it.code, true) },
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            if (list.isEmpty()) EmptySearch(viewModel.query)
            return@Column
        }

        Text(
            "Desliza una tarjeta hacia los lados para marcarla como favorita ⭐",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        if (list.isEmpty()) EmptySearch(viewModel.query)

        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(list, key = { _, c -> c.code }) { index, c ->
                val isFav = c.code in favorites
                // Si la divisa es la base, mostramos su valor contra el dólar
                val target = if (c.code == base) "USD" else base
                StaggeredAppear(index, Modifier.animateItem()) {
                    SwipeAction(
                        icon = Icons.Filled.Star,
                        label = if (isFav) "Quitar" else "Favorito",
                        color = MaterialTheme.colorScheme.tertiary,
                        onTrigger = { viewModel.toggleFavorite(c.code) }
                    ) {
                        CurrencyRow(
                            currency = c,
                            rateText = "${formatAmount(viewModel.rateInBase(c, target))} $target",
                            isFavorite = isFav,
                            onClick = { onOpen(c.code, false) },
                            onToggleFavorite = { viewModel.toggleFavorite(c.code) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearch(query: String) {
    Text(
        "No encontramos divisas para \"$query\" 🔍",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(32.dp)
    )
}
