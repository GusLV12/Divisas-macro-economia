package com.gusslinaresv.activitefinanca.ui

import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.fragment.app.Fragment
import com.gusslinaresv.activitefinanca.ui.activities.ConceptDetailActivity
import com.gusslinaresv.activitefinanca.ui.activities.CurrencyDetailActivity
import com.gusslinaresv.activitefinanca.ui.fragments.ConverterFragment
import com.gusslinaresv.activitefinanca.ui.fragments.CurrenciesFragment
import com.gusslinaresv.activitefinanca.ui.fragments.FavoritesFragment
import com.gusslinaresv.activitefinanca.ui.fragments.HomeFragment
import com.gusslinaresv.activitefinanca.ui.fragments.LearnFragment
import com.gusslinaresv.activitefinanca.ui.fragments.QuizFragment

/** Los 6 Fragments que hospeda MainActivity. El nombre del enum se usa como tag del Fragment. */
enum class MainTab(
    val label: String,
    val title: String,
    val icon: ImageVector,
    val inBottomBar: Boolean,
    val create: () -> Fragment,
) {
    HOME("Inicio", "ActiviteFinance", Icons.Filled.Home, true, ::HomeFragment),
    CURRENCIES("Divisas", "Divisas del mundo", Icons.Filled.Public, true, ::CurrenciesFragment),
    CONVERTER("Conversor", "Conversor", Icons.Filled.CurrencyExchange, true, ::ConverterFragment),
    LEARN("Aprende", "Aprende macroeconomía", Icons.Filled.School, true, ::LearnFragment),
    QUIZ("Quiz", "Pon a prueba lo aprendido", Icons.Filled.Quiz, true, ::QuizFragment),
    FAVORITES("Favoritos", "Mis favoritos", Icons.Filled.Star, false, ::FavoritesFragment),
}

/** Contrato que implementa MainActivity para que los Fragments pidan cambiar de pestaña. */
interface MainNavigator {
    fun navigateTo(tab: MainTab)
}

fun Fragment.mainNavigator(): MainNavigator? = activity as? MainNavigator

fun Context.openCurrency(code: String) = startActivity(
    Intent(this, CurrencyDetailActivity::class.java).putExtra(CurrencyDetailActivity.EXTRA_CURRENCY_CODE, code)
)

fun Context.openConcept(id: String) = startActivity(
    Intent(this, ConceptDetailActivity::class.java).putExtra(ConceptDetailActivity.EXTRA_CONCEPT_ID, id)
)
