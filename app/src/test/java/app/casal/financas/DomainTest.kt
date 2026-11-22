package app.casal.financas

import app.casal.financas.domain.Cycles
import app.casal.financas.domain.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DomainTest {
    @Test fun cycleStartingDayOne_isCalendarMonth() {
        assertEquals("2026-11", Cycles.idFor(LocalDate.of(2026, 11, 30), 1))
        assertEquals("2026-12", Cycles.idFor(LocalDate.of(2026, 12, 1), 1))
    }

    @Test fun cycleStartingDay5_splitsAroundPayday() {
        assertEquals("2026-10", Cycles.idFor(LocalDate.of(2026, 11, 4), 5))
        assertEquals("2026-11", Cycles.idFor(LocalDate.of(2026, 11, 5), 5))
        assertEquals("2025-12", Cycles.idFor(LocalDate.of(2026, 1, 2), 5))
    }

    @Test fun cycleRange() {
        assertEquals(LocalDate.of(2026, 11, 5), Cycles.start("2026-11", 5))
        assertEquals(LocalDate.of(2026, 12, 4), Cycles.end("2026-11", 5))
        assertEquals(LocalDate.of(2026, 11, 30), Cycles.end("2026-11", 1))
        assertTrue(Cycles.contains("2026-11", 5, LocalDate.of(2026, 12, 4)))
    }

    @Test fun shiftAcrossYear() {
        assertEquals("2027-01", Cycles.shift("2026-12", 1))
        assertEquals("2025-12", Cycles.shift("2026-01", -1))
    }

    @Test fun labels() {
        assertEquals("Novembro 2026", Cycles.title("2026-11"))
        assertEquals("05 nov – 04 dez", Cycles.rangeLabel("2026-11", 5))
    }

    @Test fun moneyMaskAndFormat() {
        assertEquals(0L, Money.centsFromDigits(""))
        assertEquals(123L, Money.centsFromDigits("0123"))
        assertEquals(150000L, Money.centsFromDigits("R$ 1.500,00"))
        assertEquals("R$ 1.234,56", Money.format(123456))
        assertEquals("1.234,56", Money.plain(123456))
    }
}

class SummaryTest {
    private val h = app.casal.financas.domain.Household("h", "ABC234", listOf("me", "she"), mapOf("me" to "Eu", "she" to "Ana"), 1)
    private fun e(owner: String, cents: Long, cat: app.casal.financas.domain.Category = app.casal.financas.domain.Category.MARKET) =
        app.casal.financas.domain.Expense("x$cents", "2026-11", owner, cents, "", cat, LocalDate.of(2026, 11, 3), owner)

    @Test fun summarizesPerPersonAndCategory() {
        val cycle = app.casal.financas.domain.Cycle("2026-11", mapOf("me" to 300000L, "she" to 200000L))
        val s = app.casal.financas.domain.summarize(
            h, "she", cycle,
            listOf(e("me", 10000), e("she", 25000), e("she", 5000, app.casal.financas.domain.Category.FOOD)),
            today = LocalDate.of(2026, 11, 20),
        )
        assertEquals(listOf("Você", "Eu"), s.people.map { it.name })   // "me" do ponto de vista dela é "she"
        assertEquals(500000L, s.totalBudget)
        assertEquals(40000L, s.totalSpent)
        assertEquals(460000L, s.remaining)
        assertEquals(app.casal.financas.domain.Category.MARKET, s.byCategory.first().first)
        assertEquals(460000L / 11, s.dailyAllowance)   // 20..30 nov = 11 dias
    }

    @Test fun overBudgetClampsFraction() {
        val cycle = app.casal.financas.domain.Cycle("2026-11", mapOf("me" to 1000L))
        val s = app.casal.financas.domain.summarize(h, "me", cycle, listOf(e("me", 5000)), LocalDate.of(2026, 11, 20))
        assertTrue(s.people.first().over)
        assertEquals(1f, s.people.first().fraction, 0f)
        assertEquals(null, s.dailyAllowance)
    }
}
