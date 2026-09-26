package com.gusslinaresv.activitefinanca.data

import com.gusslinaresv.activitefinanca.data.model.QuizQuestion

object QuizRepository {

    const val QUESTIONS_PER_ROUND = 10

    private val questions = listOf(
        QuizQuestion("¿Qué institución emite el peso mexicano?", listOf("Secretaría de Hacienda", "Banco de México", "Casa de Moneda de EE. UU.", "BBVA"), 1, "Banxico es el banco central de México y el único facultado para emitir billetes y monedas."),
        QuizQuestion("¿Qué moneda es la principal reserva del mundo?", listOf("Euro", "Yen", "Dólar estadounidense", "Yuan"), 2, "Cerca del 58 % de las reservas internacionales están en dólares."),
        QuizQuestion("¿En qué año empezaron a circular los billetes y monedas de euro?", listOf("1992", "1999", "2002", "2008"), 2, "El euro existió como moneda electrónica desde 1999, pero el efectivo llegó en 2002."),
        QuizQuestion("¿Cuál es la moneda más antigua todavía en uso?", listOf("Libra esterlina", "Franco suizo", "Yen", "Rupia"), 0, "La libra esterlina tiene más de 1 200 años de historia."),
        QuizQuestion("¿Qué significa \"renminbi\"?", listOf("Dinero rojo", "Moneda del pueblo", "Oro del emperador", "Papel valioso"), 1, "Renminbi significa \"moneda del pueblo\"; el yuan es su unidad."),
        QuizQuestion("Si el banco central sube la tasa de interés, normalmente…", listOf("La inflación sube", "Los créditos se abaratan", "El consumo se enfría", "Se imprime más dinero"), 2, "Tasas más altas encarecen el crédito, reducen el consumo y ayudan a bajar la inflación."),
        QuizQuestion("¿Qué mide la inflación?", listOf("El crecimiento del PIB", "El aumento generalizado de precios", "El número de desempleados", "El valor del dólar"), 1, "La inflación mide cómo suben en promedio los precios de una canasta de bienes y servicios."),
        QuizQuestion("¿Qué país creó el primer papel moneda?", listOf("Italia", "Inglaterra", "China", "Egipto"), 2, "El jiaozi circulaba en China en el siglo XI, durante la dinastía Song."),
        QuizQuestion("¿Qué moneda suele considerarse \"refugio\" en crisis?", listOf("Real brasileño", "Yen japonés", "Won surcoreano", "Peso mexicano"), 1, "El yen y el franco suizo suelen fortalecerse cuando hay pánico en los mercados."),
        QuizQuestion("¿Por qué se le dice \"loonie\" al dólar canadiense?", listOf("Por un ave en su moneda", "Por la Luna", "Por su color", "Por un banquero"), 0, "La moneda de un dólar muestra un colimbo, \"loon\" en inglés."),
        QuizQuestion("¿Qué país introdujo los primeros billetes de polímero?", listOf("Canadá", "México", "Australia", "Suiza"), 2, "Australia lanzó billetes de polímero en 1988."),
        QuizQuestion("¿Qué plan estabilizó la economía de Brasil en 1994?", listOf("Plan Marshall", "Plan Real", "Plan Austral", "Plan Cruzado"), 1, "El Plan Real introdujo la moneda actual y terminó con la hiperinflación."),
        QuizQuestion("Cuando se necesitan MENOS pesos para comprar un dólar, el peso…", listOf("Se depreció", "Se apreció", "Se devaluó por decreto", "No cambió"), 1, "Si el peso compra más dólares, ganó valor: se apreció."),
        QuizQuestion("¿Qué es un superávit comercial?", listOf("Importar más de lo que se exporta", "Exportar más de lo que se importa", "Tener deuda externa", "Tener inflación baja"), 1, "Hay superávit cuando las exportaciones superan a las importaciones."),
        QuizQuestion("¿Cuál es el código ISO del franco suizo?", listOf("SFR", "SWF", "CHF", "FRS"), 2, "CHF viene de Confoederatio Helvetica, el nombre latino de Suiza."),
        QuizQuestion("Según la regla del 72, al 8 % anual tu dinero se duplica en…", listOf("6 años", "9 años", "12 años", "18 años"), 1, "72 ÷ 8 = 9 años, aproximadamente."),
        QuizQuestion("¿Qué significa PIB?", listOf("Precio Interno Base", "Producto Interno Bruto", "Promedio de Ingresos Brutos", "Plan de Inversión Bancaria"), 1, "El PIB es el valor de todos los bienes y servicios finales producidos en un país."),
        QuizQuestion("Dos trimestres seguidos de caída del PIB se suelen llamar…", listOf("Estanflación", "Recesión técnica", "Hiperinflación", "Deflación"), 1, "Es la definición popular de recesión técnica."),
        QuizQuestion("¿Qué símbolo usa la rupia india desde 2010?", listOf("₨", "₹", "Rs$", "₽"), 1, "El símbolo ₹ se eligió mediante un concurso público de diseño."),
        QuizQuestion("¿Cuál es el banco central más antiguo del mundo?", listOf("Banco de Inglaterra", "Reserva Federal", "Riksbank de Suecia", "Banco de Francia"), 2, "El Riksbank fue fundado en 1668."),
        QuizQuestion("¿Qué país tiene las mayores reservas internacionales?", listOf("Estados Unidos", "China", "Suiza", "Japón"), 1, "China acumula más de 3 billones de dólares en reservas."),
        QuizQuestion("En 1993 México creó el \"nuevo peso\" quitándole…", listOf("Un cero", "Dos ceros", "Tres ceros", "Seis ceros"), 2, "1 000 pesos antiguos pasaron a ser 1 nuevo peso."),
        QuizQuestion("El dólar canadiense y el australiano se consideran monedas de…", listOf("Refugio", "Materias primas", "Reserva principal", "Tipo fijo"), 1, "Su valor está muy ligado a los precios de petróleo, minerales y gas."),
        QuizQuestion("¿Cómo se llama el cambio GBP/USD en la jerga de los mercados?", listOf("Cable", "Fiber", "Swissie", "Kiwi"), 0, "Por el cable telegráfico transatlántico que transmitía la cotización en el siglo XIX."),
        QuizQuestion("¿Qué pasa con tus ahorros sin invertir si hay inflación del 6 %?", listOf("Ganan valor", "Pierden poder de compra", "No les pasa nada", "Se duplican"), 1, "Con los precios subiendo, el mismo dinero compra menos cosas."),
    )

    fun newRound(): List<QuizQuestion> = questions.shuffled().take(QUESTIONS_PER_ROUND)
}
