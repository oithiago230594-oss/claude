package app.casal.financas.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.casal.financas.data.FinanceRepository
import app.casal.financas.data.ServiceLocator
import app.casal.financas.domain.Household
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Em que ponto do funil o usuário está. */
sealed interface Session {
    data object Loading : Session
    data object SignedOut : Session
    data class NeedsHousehold(val uid: String, val displayName: String) : Session
    data class Ready(val uid: String, val household: Household) : Session
}

data class ActionState(val busy: Boolean = false, val error: String? = null)

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModel(private val repo: FinanceRepository = ServiceLocator.repository) : ViewModel() {

    val session: StateFlow<Session> = repo.currentUid
        .flatMapLatest { uid ->
            if (uid == null) flowOf(Session.SignedOut)
            else repo.household(uid)
                .map<Household?, Session> { h ->
                    if (h == null) Session.NeedsHousehold(uid, repo.currentDisplayName()) else Session.Ready(uid, h)
                }
                .onStart { emit(Session.Loading) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Session.Loading)

    private val _action = MutableStateFlow(ActionState())
    val action: StateFlow<ActionState> = _action.asStateFlow()

    fun clearError() = _action.update { it.copy(error = null) }

    fun signIn(email: String, password: String) = run {
        require(email.isNotBlank() && password.isNotBlank()) { "Preencha e-mail e senha." }
        repo.signIn(email, password)
    }

    fun signUp(name: String, email: String, password: String) = run {
        require(name.isNotBlank()) { "Informe seu nome." }
        require(email.isNotBlank()) { "Informe o e-mail." }
        repo.signUp(email, password, name.trim())
    }

    fun createHousehold(uid: String, name: String) = run { repo.createHousehold(uid, name) }

    fun joinHousehold(uid: String, name: String, code: String) = run {
        require(code.trim().length == 6) { "O código tem 6 caracteres." }
        repo.joinHousehold(uid, name, code)
    }

    fun signOut() = repo.signOut()

    private fun run(block: suspend () -> Unit) {
        viewModelScope.launch {
            _action.value = ActionState(busy = true)
            _action.value = try {
                block(); ActionState()
            } catch (e: Exception) {
                ActionState(error = friendly(e))
            }
        }
    }

    private fun friendly(e: Exception): String = when (e) {
        is FirebaseAuthInvalidCredentialsException, is FirebaseAuthInvalidUserException -> "E-mail ou senha incorretos."
        is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado. Use \"Entrar\"."
        is FirebaseAuthWeakPasswordException -> "Senha fraca: use pelo menos 6 caracteres."
        is FirebaseNetworkException -> "Sem conexão com a internet."
        is IllegalArgumentException, is IllegalStateException -> e.message ?: "Algo deu errado."
        else -> e.message ?: "Algo deu errado. Tente novamente."
    }
}

private inline fun <T> MutableStateFlow<T>.update(f: (T) -> T) { value = f(value) }
