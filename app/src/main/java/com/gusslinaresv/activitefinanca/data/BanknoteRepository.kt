package com.gusslinaresv.activitefinanca.data

import androidx.compose.ui.graphics.Color
import com.gusslinaresv.activitefinanca.data.model.Currency
import com.gusslinaresv.activitefinanca.ui.components.formatAmount

/** Un billete representativo de cada divisa, para la bóveda. */
data class Banknote(
    val code: String,
    val denomination: Int,
    val color: Color,
    /** Quién (o qué) aparece en el anverso. */
    val portrait: String,
    val portraitInfo: String,
    /** Motivo del reverso. */
    val reverse: String,
    /** true = billete de polímero (plástico) con ventana transparente; false = papel de algodón con marca de agua. */
    val polymer: Boolean,
)

/** Punto de interés sobre el billete. x/y van de 0 a 1 respecto al tamaño del billete. */
data class Hotspot(val x: Float, val y: Float, val back: Boolean, val icon: String, val title: String, val text: String)

object BanknoteRepository {

    private val notes = listOf(
        Banknote("USD", 100, Color(0xFF6E8B74), "Benjamin Franklin",
            "Uno de los padres fundadores de EE. UU. Es el billete de mayor denominación en circulación.",
            "Independence Hall, en Filadelfia", polymer = false),
        Banknote("EUR", 50, Color(0xFFD9733A), "Europa (mitología griega)",
            "Los billetes de euro no muestran personas reales: en el holograma y la marca de agua aparece Europa, la princesa de la mitología griega que da nombre al continente.",
            "Un puente de estilo renacentista y el mapa de Europa", polymer = false),
        Banknote("MXN", 500, Color(0xFF2E6DB4), "Benito Juárez",
            "Presidente de México y figura de la Reforma. Aparece en el billete de 500 pesos de la familia G.",
            "La ballena gris en la Reserva de la Biosfera El Vizcaíno", polymer = false),
        Banknote("CNY", 100, Color(0xFFC8323C), "Mao Zedong",
            "Fundador de la República Popular China; aparece en todos los billetes actuales del renminbi.",
            "El Gran Salón del Pueblo, en Pekín", polymer = false),
        Banknote("JPY", 10000, Color(0xFF8A6E4B), "Eiichi Shibusawa",
            "Considerado el padre del capitalismo japonés. Su billete de 2024 trae un holograma 3D con el retrato que gira.",
            "La estación de Tokio", polymer = false),
        Banknote("GBP", 50, Color(0xFFB8455C), "El monarca británico",
            "El anverso de los billetes del Banco de Inglaterra lleva al monarca; las emisiones nuevas muestran al rey Carlos III.",
            "Alan Turing, pionero de la computación", polymer = true),
        Banknote("CHF", 100, Color(0xFF3E6FB0), "Sin personajes",
            "Los billetes suizos actuales no muestran personas: cada uno representa una faceta de Suiza. El de 100 francos está dedicado a su tradición humanitaria.",
            "Agua y manos: la tradición humanitaria suiza", polymer = false),
        Banknote("CAD", 100, Color(0xFFA0663C), "Robert Borden",
            "Primer ministro de Canadá durante la Primera Guerra Mundial.",
            "Innovación médica canadiense: la insulina y el marcapasos", polymer = true),
        Banknote("BRL", 100, Color(0xFF2F74B5), "Efigie de la República",
            "Una figura femenina que simboliza a la República; aparece en todos los billetes del real.",
            "El mero (garoupa), un pez de las costas brasileñas", polymer = false),
        Banknote("KRW", 50000, Color(0xFFD9A33B), "Shin Saimdang",
            "Artista y escritora del siglo XVI; fue la primera mujer en un billete surcoreano.",
            "Pinturas de ciruelo y bambú", polymer = false),
        Banknote("INR", 500, Color(0xFF8C8F87), "Mahatma Gandhi",
            "Líder de la independencia de la India; aparece en todos los billetes de la serie actual.",
            "El Fuerte Rojo de Delhi", polymer = false),
        Banknote("AUD", 100, Color(0xFF3E9C62), "Dame Nellie Melba",
            "Célebre cantante de ópera australiana.",
            "Sir John Monash, ingeniero y general", polymer = true),
    ).associateBy { it.code }

    fun get(code: String): Banknote = notes.getValue(code)

    /** Puntos de interés del billete. El valor se expresa también en la divisa base del usuario. */
    fun hotspots(note: Banknote, currency: Currency, baseCode: String): List<Hotspot> {
        val target = CurrencyRepository.get(if (baseCode == currency.code) (if (currency.code == "USD") "EUR" else "USD") else baseCode)
        val value = CurrencyRepository.convert(note.denomination.toDouble(), currency, target)
        return listOf(
            Hotspot(0.84f, 0.2f, back = false, icon = "👤", title = "Retrato: ${note.portrait}", text = note.portraitInfo),
            if (note.polymer) {
                Hotspot(0.2f, 0.58f, back = false, icon = "🪟", title = "Ventana transparente",
                    text = "Este billete es de polímero (un plástico muy resistente). Sus zonas transparentes son muy difíciles de falsificar y el billete dura más que uno de papel.")
            } else {
                Hotspot(0.2f, 0.58f, back = false, icon = "💡", title = "Marca de agua",
                    text = "Si pones el billete a contraluz aparece una imagen oculta en el propio papel. No está impresa: se forma al fabricar el papel, por eso una impresora no puede copiarla.")
            },
            Hotspot(0.425f, 0.3f, back = false, icon = "🌈", title = "Elemento holográfico",
                text = "Inclina el billete: los billetes modernos usan hologramas y tintas que cambian de color según el ángulo, algo que una fotocopia no puede imitar."),
            Hotspot(0.5f, 0.9f, back = false, icon = "🔍", title = "Microimpresión",
                text = "En el borde hay texto diminuto que solo se lee con lupa. Activa el modo 🔍 Lupa y pásala por la parte de abajo del billete."),
            Hotspot(0.2f, 0.62f, back = true, icon = "🏛️", title = "Reverso", text = "Muestra ${note.reverse}."),
            Hotspot(0.52f, 0.8f, back = true, icon = "💰", title = "¿Cuánto vale?",
                text = "Este billete de ${formatAmount(note.denomination.toDouble())} ${currency.code} equivale a unos ${formatAmount(value)} ${target.code} (tasas de ejemplo)."),
        )
    }
}
