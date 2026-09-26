package com.gusslinaresv.activitefinanca.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import com.gusslinaresv.activitefinanca.R
import com.gusslinaresv.activitefinanca.data.UserPreferences
import com.gusslinaresv.activitefinanca.ui.MainTab
import com.gusslinaresv.activitefinanca.ui.MainNavigator
import com.gusslinaresv.activitefinanca.ui.theme.ActiviteFinancaTheme

/**
 * Activity principal: hospeda los 6 Fragments en un FragmentContainerView y los cambia
 * con transacciones animadas. Las barras superior e inferior están hechas en Compose.
 */
class MainActivity : FragmentActivity(R.layout.activity_main), MainNavigator {

    private var currentTab by mutableStateOf(MainTab.HOME)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace(R.id.fragment_container, MainTab.HOME.create(), MainTab.HOME.name)
            }
        } else {
            currentTab = MainTab.valueOf(savedInstanceState.getString(KEY_TAB, MainTab.HOME.name))
        }

        // Al volver de Favoritos con "atrás", sincronizamos la pestaña seleccionada con el Fragment visible
        supportFragmentManager.addOnBackStackChangedListener {
            supportFragmentManager.findFragmentById(R.id.fragment_container)?.tag?.let { currentTab = MainTab.valueOf(it) }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    supportFragmentManager.backStackEntryCount > 0 -> supportFragmentManager.popBackStack()
                    currentTab != MainTab.HOME -> navigateTo(MainTab.HOME)
                    else -> finish()
                }
            }
        })

        findViewById<ComposeView>(R.id.top_bar).setContent {
            ActiviteFinancaTheme {
                MainTopBar(
                    tab = currentTab,
                    onFavorites = { navigateTo(MainTab.FAVORITES) },
                    onSettings = { startActivity(Intent(this, SettingsActivity::class.java)) }
                )
            }
        }
        findViewById<ComposeView>(R.id.bottom_bar).setContent {
            ActiviteFinancaTheme {
                MainBottomBar(selected = currentTab, onSelect = ::navigateTo)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_TAB, currentTab.name)
    }

    override fun navigateTo(tab: MainTab) {
        if (tab == currentTab) return
        val fm = supportFragmentManager

        if (tab == MainTab.FAVORITES) {
            // Favoritos se apila encima de la pestaña actual para que "atrás" regrese a ella
            fm.commit {
                setReorderingAllowed(true)
                setCustomAnimations(R.anim.slide_up_in, R.anim.fade_out, R.anim.fade_in, R.anim.slide_down_out)
                replace(R.id.fragment_container, tab.create(), tab.name)
                addToBackStack(tab.name)
            }
        } else {
            if (fm.backStackEntryCount > 0) {
                // Cerrar Favoritos; el listener actualiza currentTab con la pestaña que quedó debajo
                fm.popBackStackImmediate()
                if (tab == currentTab) return
            }
            // La dirección de la animación depende de si la nueva pestaña está a la derecha o a la izquierda
            val forward = tab.ordinal > currentTab.ordinal
            fm.commit {
                setReorderingAllowed(true)
                if (forward) setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                else setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                replace(R.id.fragment_container, tab.create(), tab.name)
            }
        }
        currentTab = tab
    }

    companion object {
        private const val KEY_TAB = "current_tab"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(tab: MainTab, onFavorites: () -> Unit, onSettings: () -> Unit) {
    val favorites by UserPreferences.favorites.collectAsState()
    TopAppBar(
        title = {
            // El título cambia con una pequeña animación vertical al cambiar de pestaña
            AnimatedContent(
                targetState = tab.title,
                transitionSpec = { (slideInVertically { it / 2 } + fadeIn()) togetherWith fadeOut() },
                label = "title"
            ) { Text(it, style = MaterialTheme.typography.titleLarge) }
        },
        actions = {
            IconButton(onClick = onFavorites) {
                BadgedBox(badge = { if (favorites.isNotEmpty()) Badge { Text("${favorites.size}") } }) {
                    Icon(
                        if (tab == MainTab.FAVORITES) Icons.Filled.Star else Icons.Outlined.StarOutline,
                        contentDescription = "Favoritos",
                        tint = if (tab == MainTab.FAVORITES) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = "Ajustes") }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
private fun MainBottomBar(selected: MainTab, onSelect: (MainTab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        MainTab.entries.filter { it.inBottomBar }.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) }
            )
        }
    }
}
