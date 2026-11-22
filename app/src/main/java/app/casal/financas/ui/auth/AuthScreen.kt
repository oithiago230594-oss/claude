package app.casal.financas.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.casal.financas.ui.ActionState
import app.casal.financas.ui.theme.PersonColors

@Composable
fun AuthScreen(
    action: ActionState,
    onSignIn: (email: String, password: String) -> Unit,
    onSignUp: (name: String, email: String, password: String) -> Unit,
) {
    var signUp by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var show by rememberSaveable { mutableStateOf(false) }

    AuthScaffold(
        title = if (signUp) "Criar conta" else "Bem-vindo de volta",
        subtitle = if (signUp) "Cada um de vocês cria a sua conta. Depois, é só ligar as duas." else "Entre para ver as finanças do casal.",
    ) {
        if (signUp) {
            OutlinedTextField(
                name, { name = it }, label = { Text("Seu nome") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
            )
        }
        OutlinedTextField(
            email, { email = it }, label = { Text("E-mail") }, singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
        )
        OutlinedTextField(
            password, { password = it }, label = { Text("Senha") }, singleLine = true,
            visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton({ show = !show }) {
                    Icon(if (show) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, "Mostrar senha")
                }
            },
            modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
        )
        ErrorText(action.error)
        Button(
            onClick = { if (signUp) onSignUp(name, email, password) else onSignIn(email, password) },
            enabled = !action.busy,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            if (action.busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
            else Text(if (signUp) "Criar conta" else "Entrar")
        }
        TextButton({ signUp = !signUp }, Modifier.align(Alignment.CenterHorizontally)) {
            Text(if (signUp) "Já tenho conta · Entrar" else "Primeira vez? Criar conta")
        }
    }
}

/** Moldura comum das telas de entrada: cabeçalho com gradiente + cartão de formulário. */
@Composable
fun AuthScaffold(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).imePadding(),
    ) {
        Box(
            Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(PersonColors.me, MaterialTheme.colorScheme.primary, PersonColors.partner)))
                .statusBarsPadding().padding(horizontal = 24.dp, vertical = 40.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(56.dp).clip(CircleShape).background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Favorite, null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(30.dp))
                }
                Text("Controle de gastos pessoais família Lopes", style = MaterialTheme.typography.headlineMedium, color = androidx.compose.ui.graphics.Color.White)
                Text("Juntos no mesmo orçamento", style = MaterialTheme.typography.bodyLarge, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.85f))
            }
        }
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(2.dp))
            content()
        }
    }
}

@Composable
fun ErrorText(error: String?) {
    if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Start)
}
