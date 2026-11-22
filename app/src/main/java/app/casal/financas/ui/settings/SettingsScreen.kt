package app.casal.financas.ui.settings

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import app.casal.financas.domain.Household
import app.casal.financas.ui.components.personColor

fun shareInvite(context: android.content.Context, code: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "Vamos controlar nossas finanças juntos! Baixe o app Finanças do Casal, crie sua conta e use o código: $code")
    }
    context.startActivity(Intent.createChooser(send, "Enviar convite"))
}

@Composable
fun SettingsScreen(
    household: Household,
    me: String,
    onCycleStartDay: (Int) -> Unit,
    onSignOut: () -> Unit,
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var confirmSignOut by remember { mutableStateOf(false) }
    var day by remember(household.cycleStartDay) { mutableFloatStateOf(household.cycleStartDay.toFloat()) }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Ajustes", style = MaterialTheme.typography.titleLarge)

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Casal", style = MaterialTheme.typography.titleMedium)
                    household.memberIds.forEach { uid ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = MaterialTheme.shapes.small, color = personColor(uid == me), modifier = Modifier.size(10.dp)) {}
                            Spacer(Modifier.width(10.dp))
                            Text(household.nameOf(uid) + if (uid == me) " (você)" else "", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    if (!household.isComplete) {
                        HorizontalDivider()
                        Text("Aguardando o outro entrar com o código:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(household.inviteCode, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                        IconButton({ clipboard.setText(AnnotatedString(household.inviteCode)) }) { Icon(Icons.Rounded.ContentCopy, "Copiar código") }
                        IconButton({ shareInvite(context, household.inviteCode) }) { Icon(Icons.Rounded.Share, "Compartilhar código") }
                    }
                }
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Início do ciclo", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "O ciclo começa todo dia ${day.toInt()} (ex.: dia do pagamento). Vale para os dois celulares.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Slider(
                        value = day, onValueChange = { day = it }, valueRange = 1f..28f, steps = 26,
                        onValueChangeFinished = { onCycleStartDay(day.toInt()) },
                    )
                }
            }

            OutlinedButton({ confirmSignOut = true }, Modifier.fillMaxWidth().height(48.dp)) { Text("Sair da conta") }
            Text(
                "Os dados ficam na nuvem (Firebase) e são sincronizados em tempo real entre os dois celulares. Lançamentos feitos sem internet são enviados assim que a conexão voltar.",
                style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (confirmSignOut) AlertDialog(
        onDismissRequest = { confirmSignOut = false },
        title = { Text("Sair da conta?") },
        text = { Text("Seus dados continuam salvos. É só entrar de novo com e-mail e senha.") },
        confirmButton = { TextButton({ confirmSignOut = false; onSignOut() }) { Text("Sair") } },
        dismissButton = { TextButton({ confirmSignOut = false }) { Text("Cancelar") } },
    )
}
