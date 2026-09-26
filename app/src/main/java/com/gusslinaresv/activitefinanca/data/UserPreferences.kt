package com.gusslinaresv.activitefinanca.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val label: String) { SYSTEM("Sistema"), LIGHT("Claro"), DARK("Oscuro") }

/**
 * Preferencias del usuario guardadas en SharedPreferences y expuestas como StateFlow,
 * para que cualquier Activity o Fragment reaccione al instante cuando cambian.
 * Se inicializa una sola vez en [com.gusslinaresv.activitefinanca.FinanceApp].
 */
object UserPreferences {

    private const val FILE = "finance_prefs"
    private const val KEY_FAVORITES = "favorites"
    private const val KEY_BASE = "base_currency"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_ONBOARDING = "onboarding_done"
    private const val KEY_QUIZ_RECORD = "quiz_record"

    private lateinit var prefs: SharedPreferences

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _baseCurrency = MutableStateFlow("MXN")
    val baseCurrency: StateFlow<String> = _baseCurrency.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _quizRecord = MutableStateFlow(0)
    val quizRecord: StateFlow<Int> = _quizRecord.asStateFlow()

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        _favorites.value = prefs.getStringSet(KEY_FAVORITES, setOf("USD", "EUR"))!!.toSet()
        _baseCurrency.value = prefs.getString(KEY_BASE, "MXN")!!
        _themeMode.value = ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name)!!)
        _quizRecord.value = prefs.getInt(KEY_QUIZ_RECORD, 0)
    }

    val onboardingDone: Boolean get() = prefs.getBoolean(KEY_ONBOARDING, false)

    fun setOnboardingDone(done: Boolean) = prefs.edit { putBoolean(KEY_ONBOARDING, done) }

    fun isFavorite(code: String): Boolean = code in _favorites.value

    fun toggleFavorite(code: String) {
        setFavorite(code, !isFavorite(code))
    }

    fun setFavorite(code: String, favorite: Boolean) {
        val updated = if (favorite) _favorites.value + code else _favorites.value - code
        _favorites.value = updated
        prefs.edit { putStringSet(KEY_FAVORITES, updated) }
    }

    fun setBaseCurrency(code: String) {
        _baseCurrency.value = code
        prefs.edit { putString(KEY_BASE, code) }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        prefs.edit { putString(KEY_THEME, mode.name) }
    }

    /** Guarda el puntaje si supera el récord. Devuelve true si es un nuevo récord. */
    fun submitQuizScore(score: Int): Boolean {
        if (score <= _quizRecord.value) return false
        _quizRecord.value = score
        prefs.edit { putInt(KEY_QUIZ_RECORD, score) }
        return true
    }

    fun resetQuizRecord() {
        _quizRecord.value = 0
        prefs.edit { putInt(KEY_QUIZ_RECORD, 0) }
    }
}
