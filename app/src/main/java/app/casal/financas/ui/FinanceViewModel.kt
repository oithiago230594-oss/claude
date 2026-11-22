package app.casal.financas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.casal.financas.data.FinanceRepository
import app.casal.financas.domain.Category
import app.casal.financas.domain.Cycle
import app.casal.financas.domain.Cycles
import app.casal.financas.domain.Expense
import app.casal.financas.domain.Household
import app.casal.financas.domain.Summary
import app.casal.financas.domain.summarize
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class SyncStatus { SYNCED, SENDING, OFFLINE }

/** Tudo que as telas financeiras precisam para um ciclo. */
data class FinanceState(
    val household: Household,
    val me: String,
    val cycleId: String,
    val isCurrentCycle: Boolean,
    val cycle: Cycle,
    val expenses: List<Expense>,
    val summary: Summary,
    val sync: SyncStatus,
)

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModel(
    private val repo: FinanceRepository,
    val me: String,
    private val household: Household,
) : ViewModel() {

    private val hid = household.id

    /** Quantos ciclos andamos para trás/frente a partir do ciclo atual. */
    private val offset = MutableStateFlow(0)

    // O casal pode mudar (parceiro(a) entra, dia do ciclo muda): escutamos o documento ao vivo.
    private val liveHousehold: StateFlow<Household> = repo.household(me)
        .filterNotNull()
        .stateIn(viewModelScope, SharingStarted.Eagerly, household)

    val state: StateFlow<FinanceState?> = combine(liveHousehold, offset) { h, off ->
        val current = Cycles.idFor(LocalDate.now(), h.cycleStartDay)
        Triple(h, Cycles.shift(current, off), off == 0)
    }.flatMapLatest { (h, cycleId, isCurrent) ->
        combine(repo.cycle(hid, cycleId), repo.expenses(hid, cycleId)) { cycle, synced ->
            FinanceState(
                household = h,
                me = me,
                cycleId = cycleId,
                isCurrentCycle = isCurrent,
                cycle = cycle,
                expenses = synced.value,
                summary = summarize(h, me, cycle, synced.value),
                sync = when {
                    synced.pendingWrites -> SyncStatus.SENDING
                    synced.fromCache -> SyncStatus.OFFLINE
                    else -> SyncStatus.SYNCED
                },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun previousCycle() { offset.value -= 1 }
    fun nextCycle() { offset.value += 1 }
    fun goToCurrentCycle() { offset.value = 0 }

    fun saveExpense(
        existing: Expense?,
        ownerUid: String,
        amountCents: Long,
        description: String,
        category: Category,
        date: LocalDate,
    ) {
        val h = liveHousehold.value
        repo.saveExpense(
            hid,
            Expense(
                id = existing?.id ?: repo.newExpenseId(hid),
                cycleId = Cycles.idFor(date, h.cycleStartDay),
                ownerUid = ownerUid,
                amountCents = amountCents,
                description = description.trim(),
                category = category,
                date = date,
                createdBy = existing?.createdBy ?: me,
            ),
        )
    }

    fun deleteExpense(id: String) = repo.deleteExpense(hid, id)

    fun saveBudgets(cycleId: String, budgets: Map<String, Long>) = repo.saveBudgets(hid, cycleId, budgets)

    /** Copia o orçamento do ciclo anterior para o ciclo informado. */
    fun copyBudgetsFromPrevious(cycleId: String, onDone: (Map<String, Long>) -> Unit) {
        viewModelScope.launch {
            val prev = runCatching { repo.cycleOnce(hid, Cycles.shift(cycleId, -1)) }.getOrNull()
            onDone(prev?.budgets.orEmpty())
        }
    }

    fun setCycleStartDay(day: Int) = repo.setCycleStartDay(hid, day)
    fun signOut() = repo.signOut()

    class Factory(
        private val repo: FinanceRepository,
        private val me: String,
        private val household: Household,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = FinanceViewModel(repo, me, household) as T
    }
}
