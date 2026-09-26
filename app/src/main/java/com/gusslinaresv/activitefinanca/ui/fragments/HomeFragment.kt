package com.gusslinaresv.activitefinanca.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.gusslinaresv.activitefinanca.data.CurrencyRepository
import com.gusslinaresv.activitefinanca.data.UserPreferences
import com.gusslinaresv.activitefinanca.ui.MainTab
import com.gusslinaresv.activitefinanca.ui.components.BanknoteViewer
import com.gusslinaresv.activitefinanca.ui.components.VaultDoor
import com.gusslinaresv.activitefinanca.ui.components.VaultWall
import com.gusslinaresv.activitefinanca.ui.mainNavigator
import com.gusslinaresv.activitefinanca.ui.openCurrency
import com.gusslinaresv.activitefinanca.viewmodel.FinanceViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Fragment 1 — Inicio como bóveda del banco: se abre la puerta, eliges una caja de seguridad,
 * la cámara se acerca, la caja se abre y sale el billete de esa divisa para explorarlo.
 */
class HomeFragment : Fragment() {

    private val viewModel: FinanceViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        themedContent {
            VaultHome(
                viewModel = viewModel,
                onLesson = { requireContext().openCurrency(it, fade = true) },
                onConvert = { code ->
                    viewModel.fromCode = code
                    val base = UserPreferences.baseCurrency.value
                    viewModel.toCode = if (base != code) base else if (code == "USD") "MXN" else "USD"
                    mainNavigator()?.navigateTo(MainTab.CONVERTER)
                },
            )
        }
}

@Composable
private fun VaultHome(viewModel: FinanceViewModel, onLesson: (String) -> Unit, onConvert: (String) -> Unit) {
    val learned by UserPreferences.learned.collectAsState()
    val base by UserPreferences.baseCurrency.collectAsState()
    val currencies = viewModel.currencies
    val day = remember { Calendar.getInstance().get(Calendar.DAY_OF_YEAR) }
    val featured = remember(day) { CurrencyRepository.featuredOfTheDay(day) }
    val scope = rememberCoroutineScope()

    var selected by rememberSaveable { mutableStateOf<Int?>(null) }
    var viewerOpen by rememberSaveable { mutableStateOf(false) }
    val zoom = remember { Animatable(if (viewerOpen) 1f else 0f) }
    val door = remember { Animatable(if (viewerOpen) 1f else 0f) }

    // Puerta de entrada: solo se anima la primera vez que se abre Inicio en la sesión
    val wheel = remember { Animatable(if (viewModel.vaultIntroShown) 1f else 0f) }
    val doorOpen = remember { Animatable(if (viewModel.vaultIntroShown) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!viewModel.vaultIntroShown) {
            delay(250)
            wheel.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
            doorOpen.animateTo(1f, tween(750, easing = FastOutSlowInEasing))
            viewModel.vaultIntroShown = true
        }
    }

    fun openBox(i: Int) {
        if (selected != null) return
        selected = i
        scope.launch {
            zoom.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
            door.animateTo(1f, tween(450))
            viewerOpen = true
        }
    }

    fun closeBox() {
        scope.launch {
            viewerOpen = false
            delay(200)
            door.animateTo(0f, tween(350))
            zoom.animateTo(0f, tween(550, easing = FastOutSlowInEasing))
            selected = null
        }
    }

    // "Atrás" cierra la caja antes de salir de Inicio
    BackHandler(enabled = selected != null) { closeBox() }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("Elige una caja de seguridad", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Cada una guarda un billete real de su divisa. Hoy destaca: ${featured.name} ✨",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.tertiaryContainer) {
                    Text(
                        "🏅 ${learned.size}/${currencies.size}",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            VaultWall(
                currencies = currencies,
                featuredCode = featured.code,
                learned = learned,
                selected = selected,
                zoom = zoom.value,
                doorOpen = door.value,
                onBoxTap = ::openBox,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }

        // Visor del billete encima de la bóveda
        AnimatedVisibility(visible = viewerOpen, enter = fadeIn(), exit = fadeOut(), modifier = Modifier.fillMaxSize()) {
            Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
                selected?.let { i ->
                    val currency = currencies[i]
                    BanknoteViewer(
                        currency = currency,
                        baseCode = base,
                        onClose = ::closeBox,
                        onLesson = { onLesson(currency.code) },
                        onConvert = { onConvert(currency.code) },
                    )
                }
            }
        }

        // Puerta de la bóveda (se puede tocar para saltar la animación)
        if (doorOpen.value < 1f) {
            VaultDoor(
                wheel = wheel.value,
                open = doorOpen.value,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        scope.launch {
                            wheel.snapTo(1f)
                            doorOpen.animateTo(1f, tween(300))
                            viewModel.vaultIntroShown = true
                        }
                    }
            )
        }
    }
}
