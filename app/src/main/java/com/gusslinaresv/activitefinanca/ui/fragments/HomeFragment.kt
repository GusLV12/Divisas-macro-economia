package com.gusslinaresv.activitefinanca.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.gusslinaresv.activitefinanca.data.ConceptRepository
import com.gusslinaresv.activitefinanca.data.CurrencyRepository
import com.gusslinaresv.activitefinanca.data.model.Currency
import com.gusslinaresv.activitefinanca.ui.MainTab
import com.gusslinaresv.activitefinanca.ui.components.ChangeChip
import com.gusslinaresv.activitefinanca.ui.components.FlagBadge
import com.gusslinaresv.activitefinanca.ui.components.SectionHeader
import com.gusslinaresv.activitefinanca.ui.components.Sparkline
import com.gusslinaresv.activitefinanca.ui.components.StaggeredAppear
import com.gusslinaresv.activitefinanca.ui.components.formatAmount
import com.gusslinaresv.activitefinanca.ui.mainNavigator
import com.gusslinaresv.activitefinanca.ui.openConcept
import com.gusslinaresv.activitefinanca.ui.openCurrency
import com.gusslinaresv.activitefinanca.ui.theme.LocalMarketColors
import com.gusslinaresv.activitefinanca.ui.theme.NumberStyle
import com.gusslinaresv.activitefinanca.viewmodel.FinanceViewModel
import java.util.Calendar

/** Fragment 1 — Inicio: divisa destacada, principales divisas, accesos rápidos y concepto del día. */
class HomeFragment : Fragment() {

    private val viewModel: FinanceViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        themedContent {
            HomeScreen(
                viewModel = viewModel,
                onOpenCurrency = { requireContext().openCurrency(it) },
                onOpenConcept = { requireContext().openConcept(it) },
                onGoTo = { mainNavigator()?.navigateTo(it) },
            )
        }
}

@Composable
private fun HomeScreen(
    viewModel: FinanceViewModel,
    onOpenCurrency: (String) -> Unit,
    onOpenConcept: (String) -> Unit,
    onGoTo: (MainTab) -> Unit,
) {
    val base by viewModel.baseCurrency.collectAsState()
    val day = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    val featured = remember(day) { CurrencyRepository.featuredOfTheDay(day) }
    val concept = remember(day) { ConceptRepository.conceptOfTheDay(day) }
    val majors = remember { listOf("USD", "EUR", "CNY", "JPY", "GBP", "MXN", "CHF").map(CurrencyRepository::get) }

    // "1 EUR = 20.10 MXN"; si la divisa es la base, se compara contra el dólar
    fun quote(c: Currency): String {
        val target = if (c.code == base) "USD" else base
        return "1 ${c.code} = ${formatAmount(viewModel.rateInBase(c, target))} $target"
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { StaggeredAppear(0) { FeaturedCard(featured, quote(featured)) { onOpenCurrency(featured.code) } } }

        item {
            StaggeredAppear(1) {
                Column {
                    SectionHeader("Principales divisas")
                    Spacer(Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        itemsIndexed(majors, key = { _, c -> c.code }) { _, c ->
                            MiniCurrencyCard(c, quote(c)) { onOpenCurrency(c.code) }
                        }
                    }
                }
            }
        }

        item {
            StaggeredAppear(2) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickAction("Convertir", "Calcula al instante", Icons.Filled.CurrencyExchange, Modifier.weight(1f)) { onGoTo(MainTab.CONVERTER) }
                    QuickAction("Jugar quiz", "10 preguntas", Icons.Filled.Quiz, Modifier.weight(1f)) { onGoTo(MainTab.QUIZ) }
                }
            }
        }

        item {
            StaggeredAppear(3) {
                Card(
                    onClick = { onOpenConcept(concept.id) },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(52.dp).background(MaterialTheme.colorScheme.secondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painterResource(concept.iconRes), contentDescription = null,
                                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSecondary),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Concepto del día", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                            Text(concept.title, style = MaterialTheme.typography.titleLarge)
                            Text(concept.shortDefinition, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }

        item { StaggeredAppear(4) { DidYouKnowCard() } }
    }
}

@Composable
private fun FeaturedCard(currency: Currency, quote: String, onClick: () -> Unit) {
    val scheme: ColorScheme = MaterialTheme.colorScheme
    Card(onClick = onClick, shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(scheme.primary, scheme.secondary)))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐ Divisa destacada del día", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.85f), modifier = Modifier.weight(1f))
                ChangeChip(currency.monthlyChangePercent, contentColor = Color.White)
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                FlagBadge(currency, size = 56.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(currency.name, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Text(currency.country, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(quote, style = MaterialTheme.typography.titleLarge.merge(NumberStyle), color = Color.White)
            Sparkline(
                // La gráfica muestra la fuerza de la divisa: invertimos "unidades por USD"
                values = currency.history.map { 1 / it },
                color = Color.White,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 10.dp)
            )
            Text(currency.summary, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.9f), modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
private fun MiniCurrencyCard(currency: Currency, quote: String, onClick: () -> Unit) {
    val market = LocalMarketColors.current
    Card(onClick = onClick, shape = RoundedCornerShape(22.dp), modifier = Modifier.width(168.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FlagBadge(currency, size = 32.dp)
                Spacer(Modifier.width(8.dp))
                Text(currency.code, style = MaterialTheme.typography.titleMedium)
            }
            Sparkline(
                values = currency.history.map { 1 / it },
                color = if (currency.monthlyChangePercent >= 0) market.gain else market.loss,
                modifier = Modifier.fillMaxWidth().height(40.dp).padding(vertical = 8.dp)
            )
            Text(quote, style = MaterialTheme.typography.labelMedium.merge(NumberStyle), maxLines = 1)
        }
    }
}

@Composable
private fun QuickAction(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick, modifier = modifier, shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Tarjeta de datos curiosos: el botón muestra otro dato con una animación de deslizamiento. */
@Composable
private fun DidYouKnowCard() {
    val facts = remember {
        CurrencyRepository.currencies.flatMap { c -> c.facts.map { "${c.code}: $it" } }.shuffled(kotlin.random.Random(7))
    }
    var index by rememberSaveable { mutableIntStateOf(0) }
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                Spacer(Modifier.width(8.dp))
                Text("¿Sabías que…?", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { index = (index + 1) % facts.size }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Otro dato")
                }
            }
            AnimatedContent(
                targetState = index,
                transitionSpec = { (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut()) },
                label = "fact"
            ) { i ->
                Text(facts[i], style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
