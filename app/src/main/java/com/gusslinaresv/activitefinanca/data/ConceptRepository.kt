package com.gusslinaresv.activitefinanca.data

import com.gusslinaresv.activitefinanca.R
import com.gusslinaresv.activitefinanca.data.model.Concept
import com.gusslinaresv.activitefinanca.data.model.SimulatorType

/** Conceptos de macroeconomía explicados de forma sencilla. */
object ConceptRepository {

    val concepts: List<Concept> = listOf(
        Concept(
            id = "inflation",
            title = "Inflación",
            iconRes = R.drawable.ic_concept_inflation,
            shortDefinition = "El aumento generalizado y sostenido de los precios. Con inflación, el mismo dinero compra menos cosas.",
            body = listOf(
                "La inflación mide cuánto suben, en promedio, los precios de una canasta de bienes y servicios que consume una familia típica. En México la mide el INEGI con el INPC; en EE. UU., el CPI.",
                "Puede surgir porque la demanda crece más rápido que la capacidad de producir (inflación de demanda), porque suben los costos como la energía (inflación de costos) o porque se imprime demasiado dinero.",
                "Un poco de inflación, alrededor del 2-3 % anual, se considera sana. Cuando se descontrola se vuelve hiperinflación, como en Alemania en 1923, Zimbabue en 2008 o Venezuela en la década de 2010.",
            ),
            example = "Si la inflación es del 5 % anual, algo que hoy cuesta $100 costará aproximadamente $105 dentro de un año. Tus ahorros sin invertir pierden poder de compra.",
            didYouKnow = listOf(
                "Zimbabue llegó a imprimir un billete de 100 billones de dólares zimbabuenses.",
                "Lo contrario de la inflación se llama deflación, y también es peligrosa porque frena el consumo.",
            ),
            simulator = SimulatorType.INFLATION,
        ),
        Concept(
            id = "interest",
            title = "Tasas de interés",
            iconRes = R.drawable.ic_concept_interest,
            shortDefinition = "El precio del dinero: lo que pagas por pedir prestado o lo que ganas por ahorrar.",
            body = listOf(
                "Los bancos centrales fijan una tasa de referencia (en México, la tasa objetivo de Banxico; en EE. UU., la tasa de fondos federales). A partir de ella se encarecen o abaratan los créditos de toda la economía.",
                "Subir las tasas enfría la economía: los créditos cuestan más, se consume menos y la inflación baja. Bajarlas la estimula: es más barato invertir y comprar.",
                "Las tasas también mueven las divisas: si un país ofrece tasas más altas, atrae capital extranjero y su moneda tiende a fortalecerse.",
            ),
            example = "Con un interés compuesto del 10 % anual, $1 000 se convierten en unos $2 594 en 10 años, porque cada año ganas intereses sobre los intereses anteriores.",
            didYouKnow = listOf(
                "La \"regla del 72\": divide 72 entre la tasa anual y sabrás en cuántos años se duplica tu dinero.",
                "Japón y la zona euro llegaron a tener tasas negativas: los bancos pagaban por guardar dinero en el banco central.",
            ),
            simulator = SimulatorType.COMPOUND_INTEREST,
        ),
        Concept(
            id = "gdp",
            title = "PIB",
            iconRes = R.drawable.ic_concept_gdp,
            shortDefinition = "Producto Interno Bruto: el valor de todos los bienes y servicios finales que produce un país en un periodo.",
            body = listOf(
                "El PIB es el termómetro principal de una economía. Se puede calcular sumando el gasto: consumo + inversión + gasto del gobierno + (exportaciones − importaciones).",
                "Cuando el PIB cae dos trimestres seguidos se suele hablar de recesión técnica. El PIB per cápita (PIB dividido entre la población) ayuda a comparar niveles de vida.",
                "El PIB no lo mide todo: no captura el trabajo doméstico no remunerado, la desigualdad ni el daño ambiental.",
            ),
            example = "Si una economía crece 3 % al año, tarda unos 24 años en duplicar su tamaño.",
            didYouKnow = listOf(
                "EE. UU., China y la zona euro juntos producen más de la mitad del PIB mundial.",
                "El concepto moderno de PIB lo desarrolló Simon Kuznets en la década de 1930.",
            ),
            simulator = SimulatorType.GDP_GROWTH,
        ),
        Concept(
            id = "exchange",
            title = "Tipo de cambio",
            iconRes = R.drawable.ic_concept_exchange,
            shortDefinition = "Cuántas unidades de una moneda necesitas para comprar una unidad de otra.",
            body = listOf(
                "El tipo de cambio puede ser flotante (lo determina la oferta y demanda, como el peso mexicano), fijo (atado a otra moneda, como el dólar de Hong Kong) o gestionado (como el yuan).",
                "Una moneda se aprecia cuando sube su valor: se necesitan menos unidades para comprar un dólar. Se deprecia cuando pierde valor.",
                "Una moneda fuerte abarata las importaciones y los viajes al extranjero, pero hace menos competitivas las exportaciones.",
            ),
            example = "Si el dólar pasa de 20 a 18 pesos, el peso se apreció: un producto de 100 USD ahora cuesta 1 800 pesos en lugar de 2 000.",
            didYouKnow = listOf(
                "El mercado de divisas (Forex) mueve más de 7 billones de dólares al día.",
                "El índice Big Mac de The Economist compara precios de hamburguesas para ver si una moneda está sobre o subvaluada.",
            ),
            simulator = SimulatorType.EXCHANGE_RATE,
        ),
        Concept(
            id = "unemployment",
            title = "Desempleo",
            iconRes = R.drawable.ic_concept_unemployment,
            shortDefinition = "El porcentaje de personas que buscan trabajo activamente y no lo encuentran.",
            body = listOf(
                "La tasa de desempleo se calcula dividiendo a los desempleados entre la población económicamente activa (quienes trabajan o buscan trabajo).",
                "Existe desempleo friccional (cambiar de trabajo toma tiempo), estructural (las habilidades no coinciden con lo que se demanda) y cíclico (por recesiones).",
                "En países como México también importa la informalidad: muchas personas trabajan sin seguridad social, lo que la tasa de desempleo no refleja.",
            ),
            example = "Si de 100 personas activas 4 buscan empleo sin encontrarlo, la tasa de desempleo es del 4 %.",
            didYouKnow = listOf(
                "La curva de Phillips sugiere que, a corto plazo, menos desempleo suele venir con más inflación.",
                "En la Gran Depresión de 1933 el desempleo en EE. UU. llegó a cerca del 25 %.",
            ),
            simulator = SimulatorType.NONE,
        ),
        Concept(
            id = "trade",
            title = "Balanza comercial",
            iconRes = R.drawable.ic_concept_trade,
            shortDefinition = "La diferencia entre lo que un país exporta y lo que importa.",
            body = listOf(
                "Si las exportaciones superan a las importaciones hay superávit comercial; si ocurre lo contrario, déficit.",
                "Un déficit no es necesariamente malo: puede reflejar que el país importa maquinaria para invertir. Pero déficits grandes y persistentes necesitan financiarse con capital extranjero.",
                "Las exportaciones generan demanda de la moneda local, por eso un superávit tiende a fortalecerla.",
            ),
            example = "Si México exporta 600 000 millones de dólares y importa 610 000 millones, tiene un déficit comercial de 10 000 millones.",
            didYouKnow = listOf(
                "China y Alemania suelen registrar grandes superávits; EE. UU., un gran déficit.",
                "Los aranceles son impuestos a las importaciones que buscan modificar la balanza comercial.",
            ),
            simulator = SimulatorType.NONE,
        ),
        Concept(
            id = "reserves",
            title = "Reservas internacionales",
            iconRes = R.drawable.ic_concept_reserves,
            shortDefinition = "Los activos en moneda extranjera y oro que guarda un banco central para respaldar su moneda.",
            body = listOf(
                "Las reservas sirven como colchón ante crisis: permiten pagar importaciones y deuda externa y, si hace falta, vender divisas para frenar una caída brusca de la moneda local.",
                "La mayor parte se guarda en dólares, seguida del euro, el yen, la libra y el yuan. Muchos bancos centrales también han aumentado sus compras de oro.",
                "China tiene las mayores reservas del mundo, con más de 3 billones de dólares.",
            ),
            example = "Si una moneda se desploma por pánico, el banco central puede vender dólares de sus reservas para aumentar la oferta y estabilizarla.",
            didYouKnow = listOf(
                "Buena parte del oro de muchos países se guarda en la bóveda de la Fed de Nueva York, a 24 metros bajo tierra.",
                "Banxico tiene reservas superiores a los 200 000 millones de dólares.",
            ),
            simulator = SimulatorType.NONE,
        ),
        Concept(
            id = "central_bank",
            title = "Bancos centrales",
            iconRes = R.drawable.ic_concept_central_bank,
            shortDefinition = "La institución que emite la moneda y dirige la política monetaria de un país.",
            body = listOf(
                "Su tarea principal suele ser mantener la inflación baja y estable. Algunos, como la Fed, también tienen el mandato de procurar el máximo empleo.",
                "Sus herramientas son la tasa de interés de referencia, las operaciones de mercado abierto (comprar o vender bonos) y, en crisis, la \"flexibilización cuantitativa\".",
                "La autonomía es clave: un banco central independiente del gobierno evita que se imprima dinero para financiar gasto, que acabaría en inflación.",
            ),
            example = "Cuando la inflación sube, el banco central aumenta la tasa de referencia; los créditos se encarecen, baja el consumo y los precios se moderan.",
            didYouKnow = listOf(
                "El Riksbank de Suecia (1668) es el banco central más antiguo del mundo.",
                "Las decisiones de la Fed pueden mover a todas las monedas del planeta en segundos.",
            ),
            simulator = SimulatorType.NONE,
        ),
    )

    private val byId = concepts.associateBy { it.id }

    fun find(id: String?): Concept? = byId[id]

    fun conceptOfTheDay(dayOfYear: Int): Concept = concepts[dayOfYear % concepts.size]
}
