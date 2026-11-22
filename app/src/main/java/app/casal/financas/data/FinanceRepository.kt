package app.casal.financas.data

import app.casal.financas.domain.Cycle
import app.casal.financas.domain.Expense
import app.casal.financas.domain.Household
import app.casal.financas.domain.Synced
import kotlinx.coroutines.flow.Flow

/** Contrato de acesso aos dados. A implementação atual usa Firebase (Auth + Firestore). */
interface FinanceRepository {
    /** uid do usuário logado, ou null. */
    val currentUid: Flow<String?>

    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String, displayName: String)
    fun signOut()
    fun currentDisplayName(): String

    /** Casal do usuário (null = ainda não criou/entrou em um). Atualiza em tempo real. */
    fun household(uid: String): Flow<Household?>
    suspend fun createHousehold(uid: String, displayName: String): Household
    suspend fun joinHousehold(uid: String, displayName: String, code: String)
    fun setCycleStartDay(householdId: String, day: Int)

    fun cycle(householdId: String, cycleId: String): Flow<Cycle>
    suspend fun cycleOnce(householdId: String, cycleId: String): Cycle
    fun saveBudgets(householdId: String, cycleId: String, budgets: Map<String, Long>)

    fun expenses(householdId: String, cycleId: String): Flow<Synced<List<Expense>>>
    fun saveExpense(householdId: String, expense: Expense)
    fun deleteExpense(householdId: String, expenseId: String)
    fun newExpenseId(householdId: String): String
}
