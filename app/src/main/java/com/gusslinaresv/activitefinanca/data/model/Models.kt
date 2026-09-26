package com.gusslinaresv.activitefinanca.data.model

import androidx.annotation.DrawableRes

enum class Region(val label: String) {
    AMERICA("América"),
    EUROPA("Europa"),
    ASIA("Asia"),
    OCEANIA("Oceanía"),
}

/**
 * Una divisa con datos de ejemplo.
 * [perUsd] = cuántas unidades de esta divisa equivalen a 1 USD.
 * [history] = 12 cierres mensuales (el último es el valor actual), también en unidades por USD.
 */
data class Currency(
    val code: String,
    val name: String,
    val nickname: String,
    val symbol: String,
    val country: String,
    val region: Region,
    @param:DrawableRes val flagRes: Int,
    val perUsd: Double,
    val centralBank: String,
    val since: Int,
    val summary: String,
    val story: String,
    val facts: List<String>,
    val history: List<Double>,
    /** % de participación en las operaciones del mercado de divisas (encuesta trienal BIS 2022, suma 200 %). */
    val tradeShare: Double,
    val milestones: List<Milestone>,
) {
    /** Variación del último mes en %, vista como fortaleza de la divisa frente al USD (positivo = se apreció). */
    val monthlyChangePercent: Double
        get() {
            val prev = history[history.lastIndex - 1]
            val now = history.last()
            return (prev / now - 1.0) * 100.0
        }
}

/** Hito histórico de una divisa, mostrado en la línea del tiempo. */
data class Milestone(val year: Int, val text: String)

data class Concept(
    val id: String,
    val title: String,
    @param:DrawableRes val iconRes: Int,
    val shortDefinition: String,
    val body: List<String>,
    val example: String,
    val didYouKnow: List<String>,
    val simulator: SimulatorType,
)

/** Mini-simulador interactivo que acompaña a cada concepto en su pantalla de detalle. */
enum class SimulatorType {
    INFLATION,
    COMPOUND_INTEREST,
    EXCHANGE_RATE,
    GDP_GROWTH,
    NONE,
}

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
)
