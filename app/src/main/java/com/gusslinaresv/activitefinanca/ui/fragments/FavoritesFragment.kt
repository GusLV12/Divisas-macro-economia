package com.gusslinaresv.activitefinanca.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.gusslinaresv.activitefinanca.R
import com.gusslinaresv.activitefinanca.ui.MainTab
import com.gusslinaresv.activitefinanca.ui.components.CurrencyRow
import com.gusslinaresv.activitefinanca.ui.components.SwipeAction
import com.gusslinaresv.activitefinanca.ui.components.formatAmount
import com.gusslinaresv.activitefinanca.ui.mainNavigator
import com.gusslinaresv.activitefinanca.ui.openCurrency
import com.gusslinaresv.activitefinanca.ui.theme.LocalMarketColors
import com.gusslinaresv.activitefinanca.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

/** Fragment 6 — Divisas favoritas: deslizar para quitar con opción de "Deshacer". */
class FavoritesFragment : Fragment() {

    private val viewModel: FinanceViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        themedContent {
            FavoritesScreen(
                viewModel = viewModel,
                onOpen = { requireContext().openCurrency(it) },
                onExplore = { mainNavigator()?.navigateTo(MainTab.CURRENCIES) }
            )
        }
}

@Composable
private fun FavoritesScreen(viewModel: FinanceViewModel, onOpen: (String) -> Unit, onExplore: () -> Unit) {
    val favorites by viewModel.favorites.collectAsState()
    val base by viewModel.baseCurrency.collectAsState()
    val list = viewModel.currencies.filter { it.code in favorites }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val loss = LocalMarketColors.current.loss

    fun remove(code: String) {
        viewModel.setFavorite(code, false)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar("$code eliminado de favoritos", actionLabel = "Deshacer", duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) viewModel.setFavorite(code, true)
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (list.isEmpty()) {
            Column(
                Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(painterResource(R.drawable.ill_empty_favorites), contentDescription = null, modifier = Modifier.size(200.dp))
                Spacer(Modifier.height(16.dp))
                Text("Aún no tienes favoritos", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Marca con ⭐ las divisas que quieras seguir de cerca y aparecerán aquí.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(Modifier.height(20.dp))
                Button(onClick = onExplore) { Text("Explorar divisas") }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text(
                        "Valores expresados en $base · desliza para quitar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(list, key = { it.code }) { c ->
                    val target = if (c.code == base) "USD" else base
                    SwipeAction(
                        icon = Icons.Filled.Delete,
                        label = "Quitar",
                        color = loss,
                        onTrigger = { remove(c.code) },
                        modifier = Modifier.animateItem()
                    ) {
                        CurrencyRow(
                            currency = c,
                            rateText = "${formatAmount(viewModel.rateInBase(c, target))} $target",
                            isFavorite = true,
                            onClick = { onOpen(c.code) },
                            onToggleFavorite = { remove(c.code) }
                        )
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}
