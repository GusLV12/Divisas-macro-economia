package com.gusslinaresv.activitefinanca.ui.fragments

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import com.gusslinaresv.activitefinanca.ui.theme.ActiviteFinancaTheme

/**
 * Crea la vista del Fragment con Compose, ya envuelta en el tema de la app.
 * Se usa desde onCreateView: `= themedContent { MiPantalla() }`.
 */
fun Fragment.themedContent(body: @Composable () -> Unit): ComposeView = content {
    ActiviteFinancaTheme {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            body()
        }
    }
}
