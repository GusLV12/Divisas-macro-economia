package com.gusslinaresv.activitefinanca.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gusslinaresv.activitefinanca.data.model.Currency
import com.gusslinaresv.activitefinanca.ui.theme.LocalMarketColors
import com.gusslinaresv.activitefinanca.ui.theme.NumberStyle

/** Bandera recortada en círculo. */
@Composable
fun FlagBadge(currency: Currency, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Image(
        painter = painterResource(currency.flagRes),
        contentDescription = "Bandera de ${currency.country}",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape)
    )
}

/** Píldora verde/roja con la variación porcentual. */
@Composable
fun ChangeChip(percent: Double, modifier: Modifier = Modifier, contentColor: Color? = null) {
    val market = LocalMarketColors.current
    val color = contentColor ?: if (percent >= 0) market.gain else market.loss
    Surface(color = color.copy(alpha = 0.14f), shape = RoundedCornerShape(50), modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)) {
            Icon(
                if (percent >= 0) Icons.Filled.ArrowUpward else Icons.Filled.ArrowDownward,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(2.dp))
            Text(formatPercent(percent), style = MaterialTheme.typography.labelMedium, color = color)
        }
    }
}

/** Estrella de favorito que "rebota" al marcarse. */
@Composable
fun FavoriteButton(isFavorite: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
        label = "favScale"
    )
    val tint by animateColorAsState(
        if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "favTint"
    )
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarOutline,
            contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
            tint = tint,
            modifier = Modifier.scale(scale)
        )
    }
}

/** Fila de divisa usada en las listas: bandera, nombre, tasa en la divisa base y variación. */
@Composable
fun CurrencyRow(
    currency: Currency,
    rateText: String,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)
        ) {
            FlagBadge(currency)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(currency.code, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        currency.symbol,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    currency.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(rateText, style = MaterialTheme.typography.titleSmall.merge(NumberStyle))
                ChangeChip(currency.monthlyChangePercent)
            }
            FavoriteButton(isFavorite, onToggleFavorite)
        }
    }
}

/** Selector compacto de divisa (bandera + código) usado en el conversor y ajustes. */
@Composable
fun CurrencyPill(currency: Currency, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.clip(RoundedCornerShape(50)).clickable(onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(6.dp, 6.dp, 14.dp, 6.dp)) {
            FlagBadge(currency, size = 28.dp)
            Spacer(Modifier.width(8.dp))
            Text(currency.code, style = MaterialTheme.typography.titleMedium)
            Text(" ▾", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
