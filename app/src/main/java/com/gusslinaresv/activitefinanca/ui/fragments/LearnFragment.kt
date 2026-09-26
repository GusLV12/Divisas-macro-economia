package com.gusslinaresv.activitefinanca.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.gusslinaresv.activitefinanca.data.ConceptRepository
import com.gusslinaresv.activitefinanca.data.model.Concept
import com.gusslinaresv.activitefinanca.ui.components.FlipCard
import com.gusslinaresv.activitefinanca.ui.components.StaggeredAppear
import com.gusslinaresv.activitefinanca.ui.openConcept

/** Fragment 4 — Catálogo de conceptos macro en tarjetas que se voltean. */
class LearnFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        themedContent {
            LearnScreen { requireContext().openConcept(it) }
        }
}

@Composable
private fun LearnScreen(onOpen: (String) -> Unit) {
    // Cada tarjeta alterna entre los tres colores de la marca
    val palettes = listOf(
        MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary,
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Text(
                "Toca una tarjeta para voltearla y ver su definición. Usa \"Leer más\" para abrir la lección completa con su simulador.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        itemsIndexed(ConceptRepository.concepts, key = { _, c -> c.id }) { i, concept ->
            val (container, accent) = palettes[i % palettes.size]
            StaggeredAppear(i) {
                FlipCard(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    front = { ConceptFront(concept, container, accent) },
                    back = { ConceptBack(concept, accent) { onOpen(concept.id) } },
                )
            }
        }
    }
}

@Composable
private fun ConceptFront(concept: Concept, container: Color, accent: Color) {
    Surface(shape = RoundedCornerShape(24.dp), color = container, modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(Modifier.size(64.dp).background(accent, CircleShape), contentAlignment = Alignment.Center) {
                Image(
                    painterResource(concept.iconRes), contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(concept.title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text("Toca para voltear ↻", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ConceptBack(concept: Concept, accent: Color, onOpen: () -> Unit) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.padding(14.dp)) {
            Text(concept.title, style = MaterialTheme.typography.titleSmall, color = accent)
            Text(
                concept.shortDefinition,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(top = 4.dp)
            )
            TextButton(onClick = onOpen, contentPadding = PaddingValues(horizontal = 4.dp)) {
                Text("Leer más")
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp).padding(start = 4.dp))
            }
        }
    }
}
