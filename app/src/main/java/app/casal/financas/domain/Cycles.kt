package app.casal.financas.domain

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Um ciclo começa no [startDay] de um mês e vai até a véspera do mesmo dia no mês seguinte.
 * Seu id é o "yyyy-MM" do mês em que começa (ex.: "2026-11").
 */
object Cycles {
    private val ptBR = Locale("pt", "BR")

    fun idFor(date: LocalDate, startDay: Int): String {
        val ym = YearMonth.from(date)
        return (if (date.dayOfMonth >= startDay) ym else ym.minusMonths(1)).toString()
    }

    fun start(cycleId: String, startDay: Int): LocalDate =
        YearMonth.parse(cycleId).atDay(startDay.coerceIn(1, 28))

    fun end(cycleId: String, startDay: Int): LocalDate = start(shift(cycleId, 1), startDay).minusDays(1)

    fun shift(cycleId: String, months: Int): String = YearMonth.parse(cycleId).plusMonths(months.toLong()).toString()

    fun contains(cycleId: String, startDay: Int, date: LocalDate): Boolean = idFor(date, startDay) == cycleId

    /** "Novembro 2026" (ciclo que começa em novembro). */
    fun title(cycleId: String): String {
        val ym = YearMonth.parse(cycleId)
        val month = ym.month.getDisplayName(TextStyle.FULL, ptBR).replaceFirstChar { it.uppercase() }
        return "$month ${ym.year}"
    }

    /** "01 nov – 30 nov". */
    fun rangeLabel(cycleId: String, startDay: Int): String {
        fun f(d: LocalDate) =
            "%02d %s".format(d.dayOfMonth, d.month.getDisplayName(TextStyle.SHORT, ptBR).trimEnd('.'))
        return "${f(start(cycleId, startDay))} – ${f(end(cycleId, startDay))}"
    }

    fun dayLabel(date: LocalDate, today: LocalDate): String = when (date) {
        today -> "Hoje"
        today.minusDays(1) -> "Ontem"
        else -> "%02d de %s".format(date.dayOfMonth, date.month.getDisplayName(TextStyle.FULL, ptBR))
    }
}
