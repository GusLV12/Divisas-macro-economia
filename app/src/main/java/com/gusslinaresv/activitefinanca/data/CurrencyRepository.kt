package com.gusslinaresv.activitefinanca.data

import com.gusslinaresv.activitefinanca.R
import com.gusslinaresv.activitefinanca.data.model.Currency
import com.gusslinaresv.activitefinanca.data.model.Milestone
import com.gusslinaresv.activitefinanca.data.model.Region
import kotlin.random.Random

/**
 * Divisas con datos locales. Las tasas son valores de ejemplo (aproximados) para fines educativos,
 * no cotizaciones en tiempo real.
 */
object CurrencyRepository {

    val months = listOf("Oct", "Nov", "Dic", "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep")

    val currencies: List<Currency> = listOf(
        Currency(
            code = "USD", name = "Dólar estadounidense", nickname = "Greenback", symbol = "$",
            country = "Estados Unidos", region = Region.AMERICA, flagRes = R.drawable.flag_usd,
            perUsd = 1.0, centralBank = "Reserva Federal (Fed)", since = 1792,
            summary = "La moneda de reserva mundial: la mayoría del comercio internacional y de las materias primas se cotiza en dólares.",
            story = "El dólar nació con la Ley de Acuñación de 1792. Tras la Segunda Guerra Mundial, los acuerdos de Bretton Woods (1944) lo convirtieron en el eje del sistema monetario internacional, atado al oro. En 1971 Estados Unidos abandonó la convertibilidad en oro y el dólar pasó a ser una moneda fiduciaria, pero conservó su papel central gracias al tamaño y profundidad de los mercados estadounidenses.",
            facts = listOf(
                "Alrededor del 58 % de las reservas de los bancos centrales del mundo están en dólares.",
                "El símbolo $ proviene del peso hispano (el \"real de a ocho\"), no del dólar.",
                "Ecuador, El Salvador y Panamá usan el dólar como moneda oficial.",
                "El petróleo y el oro se cotizan internacionalmente en dólares.",
            ),
            history = history("USD", 1.0, 0.0),
            tradeShare = 88.5,
            milestones = listOf(
                Milestone(1792, "La Ley de Acuñación crea el dólar estadounidense"),
                Milestone(1913, "Se funda la Reserva Federal"),
                Milestone(1944, "Los acuerdos de Bretton Woods lo convierten en el eje del sistema monetario, atado al oro"),
                Milestone(1971, "EE. UU. termina la convertibilidad del dólar en oro"),
            ),
        ),
        Currency(
            code = "EUR", name = "Euro", nickname = "Euro", symbol = "€",
            country = "Zona euro", region = Region.EUROPA, flagRes = R.drawable.flag_eur,
            perUsd = 0.86, centralBank = "Banco Central Europeo (BCE)", since = 1999,
            summary = "La segunda moneda más usada del mundo, compartida por más de 20 países de la Unión Europea.",
            story = "El euro se lanzó en 1999 como moneda electrónica y los billetes y monedas llegaron en 2002, sustituyendo al marco, la peseta, el franco y muchas otras. Es el mayor experimento de unión monetaria de la historia: países con economías distintas comparten una sola política monetaria dictada desde Fráncfort.",
            facts = listOf(
                "Representa cerca del 20 % de las reservas mundiales.",
                "Los billetes son iguales en todos los países, pero cada país acuña su propia cara nacional en las monedas.",
                "Los puentes y ventanas de los billetes son imaginarios para no favorecer a ningún país.",
                "Algunos países fuera de la UE, como Montenegro o Kosovo, lo usan sin ser miembros.",
            ),
            history = history("EUR", 0.86, 0.018),
            tradeShare = 30.5,
            milestones = listOf(
                Milestone(1992, "El Tratado de Maastricht fija las reglas para crear una moneda común"),
                Milestone(1999, "Nace el euro como moneda electrónica en 11 países"),
                Milestone(2002, "Llegan los billetes y monedas de euro"),
                Milestone(2023, "Croacia adopta el euro"),
            ),
        ),
        Currency(
            code = "MXN", name = "Peso mexicano", nickname = "Súper peso", symbol = "$",
            country = "México", region = Region.AMERICA, flagRes = R.drawable.flag_mxn,
            perUsd = 18.4, centralBank = "Banco de México (Banxico)", since = 1993,
            summary = "La moneda latinoamericana más negociada del mundo y una de las más líquidas entre los mercados emergentes.",
            story = "El peso desciende del real de a ocho español, acuñado en la Nueva España y usado en medio mundo durante siglos. Tras la inflación de los años 80, en 1993 se creó el \"nuevo peso\", quitándole tres ceros al anterior. Desde 1994 flota libremente y Banxico es autónomo con un objetivo de inflación del 3 %.",
            facts = listOf(
                "El real de a ocho acuñado en México fue moneda de curso legal en EE. UU. hasta 1857.",
                "El signo $ nació para abreviar \"pesos\" antes de que existiera el dólar.",
                "Las remesas y el comercio con EE. UU. hacen que el peso sea muy sensible a la economía estadounidense.",
                "Los billetes actuales son de polímero en su mayoría, más duraderos que los de papel.",
            ),
            history = history("MXN", 18.4, 0.025),
            tradeShare = 1.5,
            milestones = listOf(
                Milestone(1535, "Se funda la Casa de Moneda de México, la primera de América"),
                Milestone(1925, "Se crea el Banco de México"),
                Milestone(1993, "Nace el \"nuevo peso\", quitando tres ceros"),
                Milestone(1994, "Banxico obtiene su autonomía y el peso comienza a flotar libremente"),
            ),
        ),
        Currency(
            code = "CNY", name = "Yuan (renminbi)", nickname = "Redback", symbol = "¥",
            country = "China", region = Region.ASIA, flagRes = R.drawable.flag_cny,
            perUsd = 7.12, centralBank = "Banco Popular de China", since = 1948,
            summary = "La moneda de la segunda economía del mundo; su tipo de cambio está gestionado por el banco central.",
            story = "Renminbi significa \"moneda del pueblo\" y el yuan es su unidad. Durante décadas estuvo fijado al dólar; desde 2005 China permite que se mueva dentro de una banda diaria alrededor de un valor de referencia. En 2016 el FMI lo incluyó en la canasta de los Derechos Especiales de Giro junto al dólar, el euro, el yen y la libra.",
            facts = listOf(
                "China inventó el papel moneda: el jiaozi circulaba en el siglo XI durante la dinastía Song.",
                "Existen dos mercados: el CNY dentro de China y el CNH fuera de ella (Hong Kong).",
                "El Banco Popular de China fija cada mañana una tasa de referencia contra el dólar.",
                "China desarrolla un yuan digital (e-CNY) emitido por el banco central.",
            ),
            history = history("CNY", 7.12, 0.008),
            tradeShare = 7.0,
            milestones = listOf(
                Milestone(1024, "Aparece el jiaozi, uno de los primeros papeles moneda del mundo"),
                Milestone(1948, "Se crea el renminbi"),
                Milestone(2005, "China deja de fijar el yuan al dólar"),
                Milestone(2016, "El yuan entra en la canasta de Derechos Especiales de Giro del FMI"),
            ),
        ),
        Currency(
            code = "JPY", name = "Yen japonés", nickname = "Yen", symbol = "¥",
            country = "Japón", region = Region.ASIA, flagRes = R.drawable.flag_jpy,
            perUsd = 148.0, centralBank = "Banco de Japón (BoJ)", since = 1871,
            summary = "Moneda \"refugio\": en crisis globales los inversionistas suelen comprar yenes.",
            story = "El yen se creó en 1871 durante la modernización Meiji. Japón mantuvo tasas de interés cercanas a cero durante décadas para combatir la deflación e incluso aplicó tasas negativas entre 2016 y 2024. Esas tasas tan bajas hicieron popular el \"carry trade\": pedir prestado en yenes para invertir en monedas con tasas más altas.",
            facts = listOf(
                "Es la tercera moneda más negociada en el mercado de divisas.",
                "No tiene subdivisiones en uso: los precios se expresan en yenes enteros.",
                "Yen significa \"círculo\" u \"objeto redondo\" en japonés.",
                "Cuando hay pánico en los mercados, el yen tiende a fortalecerse.",
            ),
            history = history("JPY", 148.0, 0.028),
            tradeShare = 16.7,
            milestones = listOf(
                Milestone(1871, "La Ley de Moneda Nueva crea el yen"),
                Milestone(1882, "Se funda el Banco de Japón"),
                Milestone(1949, "El yen se fija en 360 por dólar"),
                Milestone(2016, "El Banco de Japón aplica tasas de interés negativas"),
            ),
        ),
        Currency(
            code = "GBP", name = "Libra esterlina", nickname = "Cable", symbol = "£",
            country = "Reino Unido", region = Region.EUROPA, flagRes = R.drawable.flag_gbp,
            perUsd = 0.74, centralBank = "Banco de Inglaterra (BoE)", since = 800,
            summary = "La moneda más antigua todavía en uso, con más de 1 200 años de historia.",
            story = "Su origen está en los peniques de plata anglosajones: 240 de ellos pesaban una libra de plata. Fue la moneda de reserva mundial en el siglo XIX, durante el patrón oro. El Banco de Inglaterra, fundado en 1694, es uno de los bancos centrales más antiguos del mundo.",
            facts = listOf(
                "A la cotización GBP/USD se le llama \"cable\" por el cable telegráfico transatlántico del siglo XIX.",
                "Hasta 1971 una libra tenía 20 chelines y cada chelín 12 peniques.",
                "En 1992, el \"miércoles negro\", la libra tuvo que salir del mecanismo cambiario europeo.",
                "Escocia e Irlanda del Norte tienen bancos comerciales que emiten sus propios billetes de libra.",
            ),
            history = history("GBP", 0.74, 0.017),
            tradeShare = 12.9,
            milestones = listOf(
                Milestone(800, "Surgen los peniques de plata anglosajones: 240 pesaban una libra"),
                Milestone(1694, "Se funda el Banco de Inglaterra"),
                Milestone(1971, "La libra se decimaliza: 100 peniques por libra"),
                Milestone(1992, "El \"miércoles negro\" saca a la libra del mecanismo cambiario europeo"),
            ),
        ),
        Currency(
            code = "CHF", name = "Franco suizo", nickname = "Swissie", symbol = "Fr.",
            country = "Suiza", region = Region.EUROPA, flagRes = R.drawable.flag_chf,
            perUsd = 0.80, centralBank = "Banco Nacional Suizo (BNS)", since = 1850,
            summary = "Sinónimo de estabilidad: baja inflación, finanzas públicas sólidas y fama de moneda refugio.",
            story = "El franco unificó en 1850 las muchas monedas de los cantones suizos. Su fortaleza es tal que en 2011 el banco central fijó un tope de 1,20 francos por euro para proteger a los exportadores. Cuando lo retiró por sorpresa en enero de 2015, el franco se disparó cerca de un 30 % en minutos.",
            facts = listOf(
                "Liechtenstein también usa el franco suizo.",
                "Los billetes suizos son verticales y se consideran de los más seguros del mundo.",
                "Suiza tiene una de las inflaciones más bajas de Europa desde hace décadas.",
                "El código CHF viene de Confoederatio Helvetica, el nombre latino de Suiza.",
            ),
            history = history("CHF", 0.80, 0.017),
            tradeShare = 5.2,
            milestones = listOf(
                Milestone(1850, "El franco unifica las monedas de los cantones suizos"),
                Milestone(1907, "Comienza a operar el Banco Nacional Suizo"),
                Milestone(2011, "Se fija un tope de 1.20 francos por euro"),
                Milestone(2015, "Se retira el tope y el franco se dispara"),
            ),
        ),
        Currency(
            code = "CAD", name = "Dólar canadiense", nickname = "Loonie", symbol = "C$",
            country = "Canadá", region = Region.AMERICA, flagRes = R.drawable.flag_cad,
            perUsd = 1.38, centralBank = "Banco de Canadá", since = 1858,
            summary = "Una \"moneda de materias primas\": su valor suele moverse con el precio del petróleo.",
            story = "Canadá adoptó el dólar en 1858 para facilitar el comercio con Estados Unidos. Hoy es uno de los mayores exportadores de petróleo, gas y minerales, por eso cuando suben esas materias primas el dólar canadiense tiende a fortalecerse.",
            facts = listOf(
                "Se le llama \"loonie\" por el colimbo (loon) que aparece en la moneda de 1 dólar.",
                "La moneda de 2 dólares se apoda \"toonie\".",
                "Canadá eliminó la moneda de un centavo en 2013: los pagos en efectivo se redondean.",
                "Sus billetes son de polímero desde 2011.",
            ),
            history = history("CAD", 1.38, 0.014),
            tradeShare = 6.2,
            milestones = listOf(
                Milestone(1858, "Canadá adopta el dólar"),
                Milestone(1935, "Se funda el Banco de Canadá"),
                Milestone(1987, "Aparece la moneda de un dólar con el colimbo: el \"loonie\""),
                Milestone(2011, "Llegan los billetes de polímero"),
            ),
        ),
        Currency(
            code = "BRL", name = "Real brasileño", nickname = "Real", symbol = "R$",
            country = "Brasil", region = Region.AMERICA, flagRes = R.drawable.flag_brl,
            perUsd = 5.35, centralBank = "Banco Central do Brasil", since = 1994,
            summary = "La moneda de la mayor economía de Sudamérica, nacida para vencer la hiperinflación.",
            story = "Entre 1986 y 1994 Brasil cambió de moneda varias veces por la hiperinflación, que llegó a superar el 2 000 % anual. El Plan Real de 1994 introdujo primero una unidad de cuenta (la URV) y luego el real, logrando estabilizar los precios. Es uno de los casos de estudio más famosos de política monetaria.",
            facts = listOf(
                "Antes del real, Brasil tuvo cruzeiro, cruzado, cruzado novo y cruzeiro real.",
                "El Banco Central de Brasil creó Pix, un sistema de pagos instantáneos usado por la mayoría de la población.",
                "El real es sensible a los precios de la soya, el mineral de hierro y el café.",
                "Los animales de los billetes representan la fauna brasileña, como el jaguar y el guacamayo.",
            ),
            history = history("BRL", 5.35, 0.03),
            tradeShare = 0.9,
            milestones = listOf(
                Milestone(1986, "El Plan Cruzado intenta frenar la inflación"),
                Milestone(1993, "La inflación anual supera el 2 000 %"),
                Milestone(1994, "El Plan Real crea la moneda actual y estabiliza los precios"),
                Milestone(2020, "El Banco Central lanza Pix, su sistema de pagos instantáneos"),
            ),
        ),
        Currency(
            code = "KRW", name = "Won surcoreano", nickname = "Won", symbol = "₩",
            country = "Corea del Sur", region = Region.ASIA, flagRes = R.drawable.flag_krw,
            perUsd = 1390.0, centralBank = "Banco de Corea", since = 1962,
            summary = "La moneda de una potencia exportadora de tecnología: chips, autos y barcos.",
            story = "El won actual se introdujo en 1962. En la crisis financiera asiática de 1997 perdió cerca de la mitad de su valor y Corea necesitó un rescate del FMI; muchos ciudadanos donaron sus joyas de oro para ayudar a pagar la deuda. Hoy Corea tiene grandes reservas internacionales.",
            facts = listOf(
                "Un dólar equivale a más de 1 000 wones, por eso los precios tienen muchos ceros.",
                "El won se mueve mucho con el ciclo global de semiconductores.",
                "La campaña de donación de oro de 1998 reunió más de 200 toneladas.",
                "No hay monedas en uso por debajo de 10 wones.",
            ),
            history = history("KRW", 1390.0, 0.022),
            tradeShare = 1.9,
            milestones = listOf(
                Milestone(1950, "Se funda el Banco de Corea"),
                Milestone(1962, "Se introduce el won actual"),
                Milestone(1997, "La crisis asiática hunde al won y Corea recibe un rescate del FMI"),
                Milestone(1998, "Millones de coreanos donan su oro para pagar la deuda"),
            ),
        ),
        Currency(
            code = "INR", name = "Rupia india", nickname = "Rupia", symbol = "₹",
            country = "India", region = Region.ASIA, flagRes = R.drawable.flag_inr,
            perUsd = 88.3, centralBank = "Banco de la Reserva de la India (RBI)", since = 1540,
            summary = "La moneda de la economía grande que más rápido crece y del país más poblado del mundo.",
            story = "La palabra rupia viene del sánscrito \"rupya\" (plata trabajada) y la primera rupia de plata se acuñó hacia 1540. El Banco de la Reserva de la India gestiona la moneda desde 1935. En 2016 el gobierno retiró de golpe los billetes de 500 y 1 000 rupias para combatir el dinero negro.",
            facts = listOf(
                "El símbolo ₹ se adoptó en 2010 tras un concurso público de diseño.",
                "Los billetes muestran el valor escrito en 15 idiomas.",
                "India creó UPI, uno de los sistemas de pagos digitales más usados del mundo.",
                "La rupia también circula en Bután y Nepal.",
            ),
            history = history("INR", 88.3, 0.009),
            tradeShare = 1.6,
            milestones = listOf(
                Milestone(1540, "Sher Shah Suri acuña la primera rupia de plata"),
                Milestone(1935, "Se funda el Banco de la Reserva de la India"),
                Milestone(2010, "Se adopta el símbolo ₹"),
                Milestone(2016, "Se retiran de golpe los billetes de 500 y 1 000 rupias"),
            ),
        ),
        Currency(
            code = "AUD", name = "Dólar australiano", nickname = "Aussie", symbol = "A$",
            country = "Australia", region = Region.OCEANIA, flagRes = R.drawable.flag_aud,
            perUsd = 1.52, centralBank = "Banco de la Reserva de Australia", since = 1966,
            summary = "Muy ligado a China y a las materias primas; es de las monedas más negociadas del mundo.",
            story = "Australia sustituyó la libra australiana por el dólar en 1966, adoptando el sistema decimal. Como gran exportador de hierro, carbón y gas, su moneda refleja la salud de la economía china y los ciclos de las materias primas.",
            facts = listOf(
                "Australia creó los primeros billetes de polímero del mundo en 1988.",
                "Su apodo en los mercados es \"Aussie\".",
                "Kiribati, Nauru y Tuvalu también usan el dólar australiano.",
                "Los billetes tienen una ventana transparente muy difícil de falsificar.",
            ),
            history = history("AUD", 1.52, 0.02),
            tradeShare = 6.4,
            milestones = listOf(
                Milestone(1960, "Se funda el Banco de la Reserva de Australia"),
                Milestone(1966, "El dólar australiano reemplaza a la libra"),
                Milestone(1983, "El dólar australiano comienza a flotar libremente"),
                Milestone(1988, "Australia lanza los primeros billetes de polímero del mundo"),
            ),
        ),
    )

    private val byCode = currencies.associateBy { it.code }

    fun find(code: String?): Currency? = byCode[code]

    fun get(code: String): Currency = byCode.getValue(code)

    /** Convierte [amount] de la divisa [from] a la divisa [to] usando el dólar como puente. */
    fun convert(amount: Double, from: Currency, to: Currency): Double = amount / from.perUsd * to.perUsd

    /** Divisa destacada del día: rota de forma determinista según la fecha. */
    fun featuredOfTheDay(dayOfYear: Int): Currency = currencies.filter { it.code != "USD" }.let { it[dayOfYear % it.size] }

    /**
     * Genera 12 cierres mensuales mediante una caminata aleatoria con semilla fija,
     * de modo que la gráfica sea siempre la misma y termine en el valor actual.
     */
    private fun history(code: String, current: Double, volatility: Double): List<Double> {
        val random = Random(code.hashCode())
        val values = DoubleArray(12)
        values[11] = current
        for (i in 10 downTo 0) {
            val shock = (random.nextDouble() * 2 - 1) * volatility
            values[i] = values[i + 1] * (1 + shock)
        }
        return values.toList()
    }
}
