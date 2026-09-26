package com.gusslinaresv.activitefinanca.ui.activities

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gusslinaresv.activitefinanca.data.UserPreferences
import com.gusslinaresv.activitefinanca.ui.theme.Typography
import com.gusslinaresv.activitefinanca.ui.theme.Emerald700
import com.gusslinaresv.activitefinanca.ui.theme.Gold400
import com.gusslinaresv.activitefinanca.ui.theme.Gold600
import com.gusslinaresv.activitefinanca.ui.theme.Navy900
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/** Activity 1 — Splash: moneda girando con divisas en órbita; luego decide a dónde ir. */
@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Íconos claros en la barra de estado, porque el fondo del splash es oscuro
        enableEdgeToEdge(SystemBarStyle.dark(android.graphics.Color.TRANSPARENT), SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        setContent {
            // Sin ActiviteFinancaTheme: el splash siempre es oscuro y conserva íconos claros
            MaterialTheme(typography = Typography) {
                SplashScreen(onFinished = ::goNext)
            }
        }
    }

    private fun goNext() {
        val next = if (UserPreferences.onboardingDone) MainActivity::class.java else OnboardingActivity::class.java
        // Transición de desvanecido hacia la siguiente pantalla (funciona en todas las versiones)
        val fade = ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(Intent(this, next), fade.toBundle())
        finish()
    }
}

@Composable
private fun SplashScreen(onFinished: () -> Unit) {
    val enter = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        enter.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
        textAlpha.animateTo(1f, tween(400))
        delay(700)
        onFinished()
    }
    val infinite = rememberInfiniteTransition(label = "splash")
    val spin by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(2200, easing = LinearEasing)), label = "spin")
    val orbit by infinite.animateFloat(0f, 360f, infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "orbit")
    val glow by infinite.animateFloat(0.25f, 0.5f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "glow")

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Emerald700, Navy900))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(220.dp).graphicsLayer { scaleX = enter.value; scaleY = enter.value }) {
                // Halo pulsante
                Canvas(Modifier.size(200.dp)) { drawCircle(Gold400.copy(alpha = glow), radius = size.minDimension / 2.6f) }

                // Símbolos de divisas girando alrededor de la moneda
                listOf("€", "¥", "£", "₹").forEachIndexed { i, symbol ->
                    val angle = Math.toRadians((orbit + i * 90).toDouble())
                    val r = 92
                    Text(
                        symbol,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset { IntOffset((cos(angle) * r * density).toInt(), (sin(angle) * r * density).toInt()) }
                    )
                }

                // Moneda que gira sobre su eje vertical
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp).graphicsLayer {
                        rotationY = spin
                        cameraDistance = 16 * density
                    }
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(Gold400)
                        drawCircle(Gold600, radius = size.minDimension * 0.36f, style = Stroke(width = 5.dp.toPx()))
                    }
                    Text("$", color = Gold600, fontSize = 48.sp, fontWeight = FontWeight.Black)
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "ActiviteFinance",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.graphicsLayer { alpha = textAlpha.value }
            )
            Text(
                "Divisas y economía, fácil",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.graphicsLayer { alpha = textAlpha.value }
            )
        }
    }
}
