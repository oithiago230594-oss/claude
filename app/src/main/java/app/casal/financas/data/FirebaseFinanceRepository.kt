package app.casal.financas.data

import app.casal.financas.domain.Category
import app.casal.financas.domain.Cycle
import app.casal.financas.domain.Expense
import app.casal.financas.domain.Household
import app.casal.financas.domain.Synced
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

/**
 * Estrutura no Firestore:
 *   users/{uid}                         { householdId }
 *   invites/{code}                      { householdId }
 *   households/{hid}                    { inviteCode, memberIds[], names{uid:nome}, cycleStartDay }
 *   households/{hid}/cycles/{yyyy-MM}   { budgets{uid:centavos} }
 *   households/{hid}/expenses/{id}      { cycleId, ownerUid, amountCents, description, category, date, createdBy }
 *
 * O Firestore mantém cache local: lançar gastos funciona offline e sincroniza ao reconectar.
 * Por isso as escritas de gastos/orçamento NÃO aguardam o servidor (retornam na hora).
 */
class FirebaseFinanceRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) : FinanceRepository {

    override val currentUid: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.uid) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun signUp(email: String, password: String, displayName: String) {
        val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
        result.user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(displayName).build())?.await()
    }

    override fun signOut() = auth.signOut()

    override fun currentDisplayName(): String = auth.currentUser?.displayName.orEmpty()

    // ---------- Casal ----------

    override fun household(uid: String): Flow<Household?> = callbackFlow {
        var householdListener: com.google.firebase.firestore.ListenerRegistration? = null
        val userListener = db.collection("users").document(uid).addSnapshotListener { snap, _ ->
            householdListener?.remove()
            householdListener = null
            val hid = snap?.getString("householdId")
            if (hid == null) {
                if (snap != null) trySend(null)
                return@addSnapshotListener
            }
            householdListener = db.collection("households").document(hid).addSnapshotListener { h, _ ->
                if (h != null && h.exists()) trySend(h.toHousehold())
            }
        }
        awaitClose { userListener.remove(); householdListener?.remove() }
    }

    override suspend fun createHousehold(uid: String, displayName: String): Household {
        repeat(5) {
            val code = newInviteCode()
            if (db.collection("invites").document(code).get().await().exists()) return@repeat
            val hRef = db.collection("households").document()
            val data = mapOf(
                "inviteCode" to code,
                "memberIds" to listOf(uid),
                "names" to mapOf(uid to displayName),
                "cycleStartDay" to 1,
            )
            db.runBatch { b ->
                b.set(hRef, data)
                b.set(db.collection("invites").document(code), mapOf("householdId" to hRef.id))
                b.set(db.collection("users").document(uid), mapOf("householdId" to hRef.id))
            }.await()
            return Household(hRef.id, code, listOf(uid), mapOf(uid to displayName), 1)
        }
        error("Não foi possível gerar um código de convite. Tente de novo.")
    }

    override suspend fun joinHousehold(uid: String, displayName: String, code: String) {
        val invite = db.collection("invites").document(code.trim().uppercase()).get().await()
        val hid = invite.getString("householdId") ?: throw IllegalArgumentException("Código não encontrado.")
        val ref = db.collection("households").document(hid)
        try {
            ref.update(
                mapOf(
                    "memberIds" to FieldValue.arrayUnion(uid),
                    "names.$uid" to displayName,
                ),
            ).await()
        } catch (e: Exception) {
            throw IllegalStateException("Este código já foi usado por outro casal ou é inválido.", e)
        }
        db.collection("users").document(uid).set(mapOf("householdId" to hid)).await()
    }

    override fun setCycleStartDay(householdId: String, day: Int) {
        db.collection("households").document(householdId).update("cycleStartDay", day.coerceIn(1, 28))
    }

    // ---------- Ciclo / orçamento ----------

    override fun cycle(householdId: String, cycleId: String): Flow<Cycle> = callbackFlow {
        val reg = cyclesRef(householdId).document(cycleId).addSnapshotListener { snap, _ ->
            if (snap != null) trySend(snap.toCycle(cycleId))
        }
        awaitClose { reg.remove() }
    }

    override suspend fun cycleOnce(householdId: String, cycleId: String): Cycle =
        cyclesRef(householdId).document(cycleId).get().await().toCycle(cycleId)

    override fun saveBudgets(householdId: String, cycleId: String, budgets: Map<String, Long>) {
        cyclesRef(householdId).document(cycleId).set(mapOf("budgets" to budgets), SetOptions.merge())
    }

    // ---------- Gastos ----------

    override fun expenses(householdId: String, cycleId: String): Flow<Synced<List<Expense>>> = callbackFlow {
        val reg = expensesRef(householdId).whereEqualTo("cycleId", cycleId).addSnapshotListener { snap, _ ->
            if (snap != null) {
                val list = snap.documents.mapNotNull { it.toExpense() }
                    .sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.id })
                trySend(Synced(list, snap.metadata.hasPendingWrites(), snap.metadata.isFromCache))
            }
        }
        awaitClose { reg.remove() }
    }

    override fun saveExpense(householdId: String, expense: Expense) {
        expensesRef(householdId).document(expense.id).set(
            mapOf(
                "cycleId" to expense.cycleId,
                "ownerUid" to expense.ownerUid,
                "amountCents" to expense.amountCents,
                "description" to expense.description,
                "category" to expense.category.name,
                "date" to expense.date.toString(),
                "createdBy" to expense.createdBy,
                "updatedAt" to FieldValue.serverTimestamp(),
            ),
        )
    }

    override fun deleteExpense(householdId: String, expenseId: String) {
        expensesRef(householdId).document(expenseId).delete()
    }

    override fun newExpenseId(householdId: String): String = expensesRef(householdId).document().id

    // ---------- Helpers ----------

    private fun cyclesRef(hid: String) = db.collection("households").document(hid).collection("cycles")
    private fun expensesRef(hid: String) = db.collection("households").document(hid).collection("expenses")

    private fun newInviteCode(): String {
        val alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // sem 0/O/1/I para evitar confusão
        return (1..6).map { alphabet.random() }.joinToString("")
    }

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toHousehold() = Household(
        id = id,
        inviteCode = getString("inviteCode").orEmpty(),
        memberIds = (get("memberIds") as? List<String>).orEmpty(),
        names = (get("names") as? Map<String, String>).orEmpty(),
        cycleStartDay = (getLong("cycleStartDay") ?: 1L).toInt(),
    )

    @Suppress("UNCHECKED_CAST")
    private fun DocumentSnapshot.toCycle(cycleId: String): Cycle {
        val raw = (get("budgets") as? Map<String, Any?>).orEmpty()
        return Cycle(cycleId, raw.mapValues { (it.value as? Number)?.toLong() ?: 0L })
    }

    private fun DocumentSnapshot.toExpense(): Expense? = runCatching {
        Expense(
            id = id,
            cycleId = getString("cycleId")!!,
            ownerUid = getString("ownerUid")!!,
            amountCents = getLong("amountCents")!!,
            description = getString("description").orEmpty(),
            category = Category.fromKey(getString("category")),
            date = LocalDate.parse(getString("date")!!),
            createdBy = getString("createdBy").orEmpty(),
        )
    }.getOrNull()
}
