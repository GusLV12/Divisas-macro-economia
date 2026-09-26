package com.gusslinaresv.activitefinanca.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.gusslinaresv.activitefinanca.R
import com.gusslinaresv.activitefinanca.data.UserPreferences
import com.gusslinaresv.activitefinanca.ui.theme.ActiviteFinancaTheme
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/** Activity 2 — Introducción de 3 páginas que se muestra solo la primera vez. */
class OnboardingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ActiviteFinancaTheme {
                OnboardingScreen(onDone = ::finishOnboarding)
            }
        }
    }

    private fun finishOnboarding() {
        UserPreferences.setOnboardingDone(true)
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish()
    }
}

private data class OnboardingPage(@param:DrawableRes val image: Int, val title: String, val text: String)

private val pages = listOf(
    OnboardingPage(R.drawable.ill_onboarding_1, "Las divisas del mundo", "Conoce el dólar, el euro, el peso, el yuan, el yen y más: su historia, su banco central y datos curiosos."),
    OnboardingPage(R.drawable.ill_onboarding_2, "Convierte y compara", "Calcula equivalencias al instante y explora cómo ha cambiado cada divisa en el último año."),
    OnboardingPage(R.drawable.ill_onboarding_3, "Aprende jugando", "Entiende la inflación, las tasas de interés o el PIB con simuladores y pon a prueba lo aprendido en el quiz."),
)

@Composable
private fun OnboardingScreen(onDone: () -> Unit) {
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val isLast = pager.currentPage == pages.lastIndex

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .padding(24.dp)
    ) {
        Row(Modifier.fillMaxWidth().height(48.dp), horizontalArrangement = Arrangement.End) {
            if (!isLast) TextButton(onClick = onDone) { Text("Saltar") }
        }
        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { page ->
            PageContent(pages[page], page, pager)
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) {
            repeat(pages.size) { i -> Dot(selected = i == pager.currentPage) }
        }
        Button(
            onClick = { if (isLast) onDone() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } },
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) {
            Text(if (isLast) "Comenzar" else "Siguiente")
        }
    }
}

@Composable
private fun PageContent(page: OnboardingPage, index: Int, pager: PagerState) {
    // Efecto parallax: la ilustración se escala y se desvanece según lo lejos que esté la página.
    // Se lee dentro de graphicsLayer (fase de dibujo) para no recomponer en cada píxel del deslizamiento.
    fun offset() = ((pager.currentPage - index) + pager.currentPageOffsetFraction).absoluteValue.coerceIn(0f, 1f)
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painterResource(page.image), contentDescription = null,
            modifier = Modifier.size(260.dp).graphicsLayer {
                val offset = offset()
                val s = lerp(1f, 0.7f, offset)
                scaleX = s; scaleY = s
                alpha = lerp(1f, 0.3f, offset)
                rotationZ = lerp(0f, 12f, offset)
            }
        )
        Spacer(Modifier.height(32.dp))
        Text(page.title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(
            page.text,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.graphicsLayer { alpha = 1f - offset() }
        )
    }
}

@Composable
private fun Dot(selected: Boolean) {
    val width by animateDpAsState(if (selected) 28.dp else 10.dp, label = "dotWidth")
    val color by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        label = "dotColor"
    )
    Box(
        Modifier
            .padding(horizontal = 4.dp)
            .height(10.dp)
            .width(width)
            .background(color, RoundedCornerShape(50))
    )
}
