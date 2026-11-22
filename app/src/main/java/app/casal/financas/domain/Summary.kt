package app.casal.financas.domain

import java.time.LocalDate

data class PersonSummary(
    val uid: String,
    val name: String,
    val isMe: Boolean,
    val budget: Long,
    val spent: Long,
) {
    val remaining: Long get() = budget - spent
    val over: Boolean get() = spent > budget
    val fraction: Float
        get() = if (budget <= 0L) (if (spent > 0L) 1f else 0f) else (spent.toDouble() / budget).toFloat().coerceIn(0f, 1f)
}

data class Summary(
    val people: List<PersonSummary>,
    val byCategory: List<Pair<Category, Long>>,
    /** Quanto ainda dá para gastar por dia até o fim do ciclo (null se o ciclo já acabou ou não há saldo). */
    val dailyAllowance: Long?,
) {
    val totalBudget: Long get() = people.sumOf { it.budget }
    val totalSpent: Long get() = people.sumOf { it.spent }
    val remaining: Long get() = totalBudget - totalSpent
    val over: Boolean get() = totalSpent > totalBudget
    val hasBudget: Boolean get() = totalBudget > 0L
    val fraction: Float
        get() = if (totalBudget <= 0L) (if (totalSpent > 0L) 1f else 0f)
        else (totalSpent.toDouble() / totalBudget).toFloat().coerceIn(0f, 1f)
}

fun summarize(
    household: Household,
    me: String,
    cycle: Cycle,
    expenses: List<Expense>,
    today: LocalDate = LocalDate.now(),
): Summary {
    // Eu primeiro, depois o(a) parceiro(a).
    val order = household.memberIds.sortedBy { if (it == me) 0 else 1 }
    val people = order.map { uid ->
        PersonSummary(
            uid = uid,
            name = if (uid == me) "Você" else household.nameOf(uid),
            isMe = uid == me,
            budget = cycle.budgets[uid] ?: 0L,
            spent = expenses.filter { it.ownerUid == uid }.sumOf { it.amountCents },
        )
    }
    val byCategory = expenses.groupBy { it.category }
        .map { (cat, list) -> cat to list.sumOf { it.amountCents } }
        .sortedByDescending { it.second }

    val end = Cycles.end(cycle.id, household.cycleStartDay)
    val remaining = people.sumOf { it.remaining }
    val daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, end).toInt() + 1
    val start = Cycles.start(cycle.id, household.cycleStartDay)
    val allowance = if (today >= start && daysLeft >= 1 && remaining > 0) remaining / daysLeft else null
    return Summary(people, byCategory, allowance)
}
