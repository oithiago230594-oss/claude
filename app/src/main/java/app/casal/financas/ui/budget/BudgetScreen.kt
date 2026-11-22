package app.casal.financas.ui.budget

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.casal.financas.domain.Money
import app.casal.financas.ui.FinanceState
import app.casal.financas.ui.components.MoneyField
import app.casal.financas.ui.components.personColor
import app.casal.financas.ui.home.CycleSwitcher

/** Onde se define, no início do ciclo, quanto cada um pode gastar. */
@Composable
fun BudgetScreen(
    state: FinanceState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onSave: (Map<String, Long>) -> Unit,
    onCopyPrevious: ((Map<String, Long>) -> Unit) -> Unit,
) {
    val uids = state.summary.people.map { it.uid }
    var values by remember(state.cycleId, state.cycle.budgets, uids) {
        mutableStateOf(uids.associateWith { state.cycle.budgets[it] ?: 0L })
    }
    var savedMessage by remember(state.cycleId) { mutableStateOf(false) }
    val total = values.values.sum()
    val changed = values != uids.associateWith { state.cycle.budgets[it] ?: 0L }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).imePadding().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Orçamento do ciclo", style = MaterialTheme.typography.titleLarge)
            Text(
                "Defina quanto cada um pode gastar neste período. Os dois celulares veem o mesmo valor.",
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CycleSwitcher(state, onPrev, onNext, onToday)

            state.summary.people.forEach { p ->
                MoneyField(
                    cents = values[p.uid] ?: 0L,
                    onChange = { v -> values = values + (p.uid to v); savedMessage = false },
                    label = "Valor de ${if (p.isMe) "você" else p.name}",
                    accent = personColor(p.isMe),
                )
            }

            ElevatedCard(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp)) {
                    Text("Total do casal", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Text(Money.format(total), style = MaterialTheme.typography.titleMedium)
                }
            }

            Button(
                onClick = { onSave(values); savedMessage = true },
                enabled = changed,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(if (savedMessage && !changed) "Salvo ✓" else "Salvar orçamento") }

            OutlinedButton(
                onClick = { onCopyPrevious { prev -> values = uids.associateWith { prev[it] ?: values[it] ?: 0L }; savedMessage = false } },
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Icon(Icons.Rounded.ContentCopy, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Copiar valores do ciclo anterior")
            }
        }
    }
}
