package app.casal.financas.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import app.casal.financas.ui.ActionState

/** Primeira configuração: um do casal cria o espaço; o outro entra com o código de 6 letras. */
@Composable
fun PairingScreen(
    displayName: String,
    action: ActionState,
    onCreate: () -> Unit,
    onJoin: (code: String) -> Unit,
    onSignOut: () -> Unit,
) {
    var code by rememberSaveable { mutableStateOf("") }
    AuthScaffold(
        title = "Olá, ${displayName.ifBlank { "tudo bem" }}!",
        subtitle = "Vamos ligar vocês dois. Um cria o espaço do casal e o outro entra com o código.",
    ) {
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Sou o primeiro a configurar", style = MaterialTheme.typography.titleMedium)
                Text("Você recebe um código para passar à sua esposa/marido.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Button(onCreate, enabled = !action.busy, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Criar espaço do casal") }
            }
        }
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Já recebi um código", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    code, { code = it.filter(Char::isLetterOrDigit).uppercase().take(6) },
                    label = { Text("Código de 6 caracteres") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
                )
                FilledTonalButton({ onJoin(code) }, enabled = !action.busy && code.length == 6, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text("Entrar no casal")
                }
            }
        }
        if (action.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        ErrorText(action.error)
        TextButton(onSignOut, Modifier.align(Alignment.CenterHorizontally)) { Text("Sair da conta") }
    }
}
