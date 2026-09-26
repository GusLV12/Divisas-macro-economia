package com.gusslinaresv.activitefinanca.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gusslinaresv.activitefinanca.data.ConceptRepository
import com.gusslinaresv.activitefinanca.data.model.Concept
import com.gusslinaresv.activitefinanca.ui.components.ConceptSimulator
import com.gusslinaresv.activitefinanca.ui.components.DetailTopBar
import com.gusslinaresv.activitefinanca.ui.components.Reveal
import com.gusslinaresv.activitefinanca.ui.theme.ActiviteFinancaTheme

/** Activity 5 — Lección de un concepto macroeconómico con ejemplo y simulador. */
class ConceptDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val concept = ConceptRepository.find(intent.getStringExtra(EXTRA_CONCEPT_ID))
        if (concept == null) {
            finish()
            return
        }
        setContent {
            ActiviteFinancaTheme {
                ConceptDetailScreen(concept, onBack = ::finish)
            }
        }
    }

    companion object {
        const val EXTRA_CONCEPT_ID = "extra_concept_id"
    }
}

@Composable
private fun ConceptDetailScreen(concept: Concept, onBack: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val float by rememberInfiniteTransition(label = "icon").animateFloat(
        -6f, 6f, infiniteRepeatable(tween(1400), RepeatMode.Reverse), label = "iconFloat"
    )

    Scaffold(topBar = { DetailTopBar(concept.title, onBack) }, containerColor = scheme.background) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = padding.calculateTopPadding() + 4.dp, bottom = padding.calculateBottomPadding() + 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(scheme.secondary, scheme.primary)), RoundedCornerShape(28.dp))
                        .padding(22.dp)
                ) {
                    Column {
                        // El ícono "flota" suavemente
                        Box(
                            Modifier
                                .size(64.dp)
                                .graphicsLayer { translationY = float * density }
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(painterResource(concept.iconRes), contentDescription = null, colorFilter = ColorFilter.tint(Color.White), modifier = Modifier.size(36.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(concept.title, style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        Text(concept.shortDefinition, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.92f))
                    }
                }
            }

            items(concept.body) { paragraph ->
                Reveal { Text(paragraph, style = MaterialTheme.typography.bodyLarge) }
            }

            item {
                Reveal { Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = scheme.tertiaryContainer)) {
                    Column(Modifier.padding(18.dp)) {
                        Text("📌 Ejemplo práctico", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(concept.example, style = MaterialTheme.typography.bodyLarge)
                    }
                } }
            }

            item { Reveal { ConceptSimulator(concept.simulator) } }

            item { Text("¿Sabías que…?", style = MaterialTheme.typography.titleLarge) }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(concept.didYouKnow) { fact ->
                        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.width(260.dp)) {
                            Text("💡 $fact", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(16.dp).fillMaxSize())
                        }
                    }
                }
            }
        }
    }
}
