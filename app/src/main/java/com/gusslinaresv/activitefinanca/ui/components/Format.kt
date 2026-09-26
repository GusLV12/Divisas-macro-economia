package com.gusslinaresv.activitefinanca.ui.components

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

private val locale: Locale = Locale.forLanguageTag("es-MX")

/** Formatea cantidades eligiendo decimales según la magnitud: 0.0123 → "0.0123", 18.4 → "18.40", 1390 → "1,390.00". */
fun formatAmount(value: Double): String {
    val decimals = when {
        value == 0.0 -> 2
        abs(value) < 0.01 -> 5
        abs(value) < 1 -> 4
        else -> 2
    }
    return NumberFormat.getNumberInstance(locale).apply {
        minimumFractionDigits = decimals
        maximumFractionDigits = decimals
    }.format(value)
}

fun formatPercent(value: Double): String {
    val sign = if (value > 0) "+" else ""
    return sign + String.format(locale, "%.2f %%", value)
}
