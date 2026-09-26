package com.gusslinaresv.activitefinanca.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.gusslinaresv.activitefinanca.data.model.Currency
import kotlinx.coroutines.delay

/** Número que "cuenta" suavemente hasta su nuevo valor cada vez que cambia. */
@Composable
fun AnimatedNumber(value: Double, style: TextStyle, modifier: Modifier = Modifier, prefix: String = "", from: Double = value) {
    // [from] permite que la primera vez cuente desde otro valor (p. ej. desde 0)
    val animated = remember { Animatable(from.toFloat()) }
    LaunchedEffect(value) { animated.animateTo(value.toFloat(), tween(600, easing = FastOutSlowInEasing)) }
    // Al terminar mostramos el valor exacto (Double) para no perder precisión por el Float de la animación
    val shown = if (animated.isRunning) animated.value.toDouble() else value
    Text(prefix + formatAmount(shown), style = style, modifier = modifier)
}

/** Aparición escalonada: cada elemento entra deslizándose un poco después del anterior. */
@Composable
fun StaggeredAppear(index: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index.coerceAtMost(8) * 55L)
        progress.animateTo(1f, tween(380, easing = FastOutSlowInEasing))
    }
    Box(
        modifier.graphicsLayer {
            alpha = progress.value
            translationY = (1f - progress.value) * 40.dp.toPx()
        }
    ) { content() }
}

/** Tarjeta que se voltea en 3D al tocarla, mostrando [front] o [back]. */
@Composable
fun FlipCard(
    modifier: Modifier = Modifier,
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
) {
    var flipped by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(if (flipped) 180f else 0f, tween(500), label = "flip")
    Box(
        modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12 * density
            }
            .clickable { flipped = !flipped }
    ) {
        if (rotation <= 90f) {
            front()
        } else {
            // La cara trasera se voltea de nuevo para que el texto no quede en espejo
            Box(Modifier.graphicsLayer { rotationY = 180f }) { back() }
        }
    }
}

/** Sección con título que se expande/contrae con animación. */
@Composable
fun ExpandableSection(
    title: String,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val arrow by animateFloatAsState(if (expanded) 180f else 0f, label = "arrow")
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp, modifier = modifier.fillMaxWidth()) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 18.dp, vertical = 16.dp)
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.rotate(arrow))
            }
            AnimatedVisibility(visible = expanded, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                Box(Modifier.padding(start = 18.dp, end = 18.dp, bottom = 18.dp)) { content() }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        action?.invoke()
    }
}

/** Barra superior con botón "atrás" para las Activities de detalle. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailTopBar(title: String, onBack: () -> Unit, actions: @Composable () -> Unit = {}) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás") }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

/** Hoja inferior para elegir una divisa. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerSheet(
    currencies: List<Currency>,
    selectedCode: String,
    onPick: (Currency) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text("Elige una divisa", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 24.dp))
        LazyColumn(contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)) {
            items(currencies, key = { it.code }) { c ->
                ListItem(
                    leadingContent = { FlagBadge(c, size = 36.dp) },
                    headlineContent = { Text("${c.code} · ${c.name}") },
                    supportingContent = { Text(c.country) },
                    trailingContent = { if (c.code == selectedCode) Text("✓", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onPick(c) }
                )
            }
        }
    }
}

@Composable
fun Bullet(text: String, modifier: Modifier = Modifier) {
    Row(modifier) {
        Text("•", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}
