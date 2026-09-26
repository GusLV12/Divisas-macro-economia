package com.gusslinaresv.activitefinanca.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gusslinaresv.activitefinanca.data.CurrencyRepository
import com.gusslinaresv.activitefinanca.data.ThemeMode
import com.gusslinaresv.activitefinanca.data.UserPreferences
import com.gusslinaresv.activitefinanca.ui.components.CurrencyPickerSheet
import com.gusslinaresv.activitefinanca.ui.components.CurrencyPill
import com.gusslinaresv.activitefinanca.ui.components.CurrencyRow
import com.gusslinaresv.activitefinanca.ui.components.DetailTopBar
import com.gusslinaresv.activitefinanca.ui.components.formatAmount
import com.gusslinaresv.activitefinanca.ui.theme.ActiviteFinancaTheme

/** Activity 6 — Ajustes: divisa base, tema, reinicios y "Acerca de". */
class SettingsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ActiviteFinancaTheme {
                SettingsScreen(
                    onBack = ::finish,
                    onReplayOnboarding = {
                        UserPreferences.setOnboardingDone(false)
                        startActivity(Intent(this, OnboardingActivity::class.java))
                    },
                    onToast = { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(onBack: () -> Unit, onReplayOnboarding: () -> Unit, onToast: (String) -> Unit) {
    val baseCode by UserPreferences.baseCurrency.collectAsState()
    val themeMode by UserPreferences.themeMode.collectAsState()
    val record by UserPreferences.quizRecord.collectAsState()
    val favorites by UserPreferences.favorites.collectAsState()
    var showPicker by remember { mutableStateOf(false) }
    var confirmReset by remember { mutableStateOf(false) }
    val base = CurrencyRepository.get(baseCode)
    val preview = CurrencyRepository.get(if (baseCode == "EUR") "USD" else "EUR")

    Scaffold(topBar = { DetailTopBar("Ajustes", onBack) }, containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsGroup("Divisa base") {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Las tasas de Inicio, Divisas y Favoritos se muestran en esta divisa.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    CurrencyPill(base, onClick = { showPicker = true })
                    Spacer(Modifier.height(12.dp))
                    Text("Vista previa", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    CurrencyRow(
                        currency = preview,
                        rateText = "${formatAmount(CurrencyRepository.convert(1.0, preview, base))} ${base.code}",
                        isFavorite = preview.code in favorites,
                        onClick = {},
                        onToggleFavorite = { UserPreferences.toggleFavorite(preview.code) }
                    )
                }
            }

            SettingsGroup("Tema") {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(16.dp)) {
                    ThemeMode.entries.forEachIndexed { i, mode ->
                        SegmentedButton(
                            selected = themeMode == mode,
                            onClick = { UserPreferences.setThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(i, ThemeMode.entries.size),
                            icon = {
                                Icon(
                                    when (mode) {
                                        ThemeMode.SYSTEM -> Icons.Filled.PhoneAndroid
                                        ThemeMode.LIGHT -> Icons.Filled.LightMode
                                        ThemeMode.DARK -> Icons.Filled.DarkMode
                                    },
                                    contentDescription = null
                                )
                            }
                        ) { Text(mode.label) }
                    }
                }
            }

            SettingsGroup("Progreso") {
                Column {
                    ListItem(
                        leadingContent = { Icon(Icons.Filled.EmojiEvents, contentDescription = null) },
                        headlineContent = { Text("Récord del quiz") },
                        supportingContent = { Text("$record de 10 aciertos") },
                        trailingContent = { TextButton(onClick = { confirmReset = true }, enabled = record > 0) { Text("Reiniciar") } },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                    ListItem(
                        leadingContent = { Icon(Icons.Filled.Replay, contentDescription = null) },
                        headlineContent = { Text("Ver la introducción otra vez") },
                        trailingContent = { TextButton(onClick = onReplayOnboarding) { Text("Abrir") } },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            }

            SettingsGroup("Acerca de") {
                ListItem(
                    leadingContent = { Icon(Icons.Filled.Info, contentDescription = null) },
                    headlineContent = { Text("ActiviteFinance 1.0") },
                    supportingContent = {
                        Text("App educativa sobre divisas y macroeconomía. Las tasas son valores de ejemplo, no cotizaciones reales. Banderas e ilustraciones generadas como gráficos vectoriales.")
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                )
            }
        }
    }

    if (showPicker) {
        CurrencyPickerSheet(
            currencies = CurrencyRepository.currencies,
            selectedCode = baseCode,
            onPick = {
                UserPreferences.setBaseCurrency(it.code)
                showPicker = false
                onToast("Divisa base: ${it.name}")
            },
            onDismiss = { showPicker = false }
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("¿Reiniciar récord?") },
            text = { Text("Tu récord de $record aciertos volverá a 0.") },
            confirmButton = {
                TextButton(onClick = {
                    UserPreferences.resetQuizRecord()
                    confirmReset = false
                }) { Text("Reiniciar") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}
