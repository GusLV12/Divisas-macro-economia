package com.gusslinaresv.activitefinanca.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.gusslinaresv.activitefinanca.data.CurrencyRepository
import com.gusslinaresv.activitefinanca.data.UserPreferences
import com.gusslinaresv.activitefinanca.data.model.Currency
import com.gusslinaresv.activitefinanca.data.model.Region
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel compartido por los Fragments de MainActivity (se obtiene con activityViewModels()).
 * Guarda el estado que debe sobrevivir a rotaciones y a los cambios de pestaña.
 */
class FinanceViewModel : ViewModel() {

    val currencies: List<Currency> = CurrencyRepository.currencies
    val favorites: StateFlow<Set<String>> = UserPreferences.favorites
    val baseCurrency: StateFlow<String> = UserPreferences.baseCurrency

    // --- Lista de divisas ---
    var query by mutableStateOf("")
    var region by mutableStateOf<Region?>(null)

    fun filtered(): List<Currency> = currencies.filter { c ->
        (region == null || c.region == region) &&
            (query.isBlank() || listOf(c.code, c.name, c.country, c.nickname).any { it.contains(query.trim(), ignoreCase = true) })
    }

    fun toggleFavorite(code: String) = UserPreferences.toggleFavorite(code)

    fun setFavorite(code: String, favorite: Boolean) = UserPreferences.setFavorite(code, favorite)

    /** true cuando la puerta de la bóveda de Inicio ya se animó en esta sesión. */
    var vaultIntroShown = false

    /** true = selección con burbujas flotantes; false = lista clásica. */
    var bubbleMode by mutableStateOf(true)

    // --- Conversor ---
    var fromCode by mutableStateOf("USD")
    var toCode by mutableStateOf(UserPreferences.baseCurrency.value.takeIf { it != "USD" } ?: "MXN")
    var amountText by mutableStateOf("100")

    val amount: Double get() = amountText.toDoubleOrNull() ?: 0.0

    fun converted(): Double = CurrencyRepository.convert(amount, CurrencyRepository.get(fromCode), CurrencyRepository.get(toCode))

    fun swap() {
        val tmp = fromCode
        fromCode = toCode
        toCode = tmp
    }

    /** Entrada del teclado numérico propio del conversor. */
    fun onKey(key: String) {
        amountText = when (key) {
            "⌫" -> amountText.dropLast(1).ifEmpty { "0" }
            "C" -> "0"
            "." -> if ('.' in amountText) amountText else "$amountText."
            else -> when {
                amountText == "0" -> key
                amountText.substringAfter('.', "").length >= 2 && '.' in amountText -> amountText
                amountText.length >= 12 -> amountText
                else -> amountText + key
            }
        }
    }

    /** Tasa de [currency] expresada en la divisa base del usuario (p. ej. 1 USD = 18.40 MXN). */
    fun rateInBase(currency: Currency, baseCode: String): Double =
        CurrencyRepository.convert(1.0, currency, CurrencyRepository.get(baseCode))
}
