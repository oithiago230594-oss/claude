package app.casal.financas.domain

import java.time.LocalDate

/** O casal. [names] mapeia uid -> nome de exibição. */
data class Household(
    val id: String,
    val inviteCode: String,
    val memberIds: List<String>,
    val names: Map<String, String>,
    /** Dia do mês em que o ciclo começa (1..28), ex.: dia do pagamento. */
    val cycleStartDay: Int,
) {
    val isComplete: Boolean get() = memberIds.size >= 2
    fun partnerOf(uid: String): String? = memberIds.firstOrNull { it != uid }
    fun nameOf(uid: String?): String = names[uid] ?: "—"
}

/** Orçamento de um ciclo: uid -> valor em centavos. */
data class Cycle(val id: String, val budgets: Map<String, Long>) {
    val total: Long get() = budgets.values.sum()
}

data class Expense(
    val id: String,
    val cycleId: String,
    val ownerUid: String,
    val amountCents: Long,
    val description: String,
    val category: Category,
    val date: LocalDate,
    val createdBy: String,
)

/** Resultado de uma escuta em tempo real, com o estado de sincronização. */
data class Synced<T>(val value: T, val pendingWrites: Boolean, val fromCache: Boolean)
