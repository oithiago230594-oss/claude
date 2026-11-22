package app.casal.financas.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.casal.financas.domain.Cycles
import app.casal.financas.domain.Money
import app.casal.financas.ui.FinanceState
import app.casal.financas.ui.components.EmptyState
import app.casal.financas.ui.components.ExpenseRow
import java.time.LocalDate

@Composable
fun HistoryScreen(
    state: FinanceState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenExpense: (String) -> Unit,
) {
    var filter by rememberSaveable { mutableStateOf<String?>(null) } // null = todos, senão uid
    val list = state.expenses.filter { filter == null || it.ownerUid == filter }
    val today = LocalDate.now()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = { FloatingActionButton(onAddExpense) { Icon(Icons.Rounded.Add, "Novo gasto") } },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { Text("Gastos", style = MaterialTheme.typography.titleLarge) }
            item { CycleSwitcher(state, onPrev, onNext, onToday) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(filter == null, { filter = null }, label = { Text("Todos") })
                    state.summary.people.forEach { p ->
                        FilterChip(filter == p.uid, { filter = p.uid }, label = { Text(p.name) })
                    }
                }
            }
            item {
                Text(
                    "${list.size} ${if (list.size == 1) "gasto" else "gastos"} · ${Money.format(list.sumOf { it.amountCents })}",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (list.isEmpty()) {
                item { EmptyState(Icons.Rounded.Receipt, "Nada por aqui", "Nenhum gasto neste ciclo com esse filtro.") }
            } else {
                list.groupBy { it.date }.forEach { (date, items) ->
                    item(key = "h$date") {
                        Text(
                            Cycles.dayLabel(date, today), style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    item(key = "c$date") {
                        ElevatedCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                                items.forEach { e -> ExpenseRow(e, nameFor(state, e.ownerUid), e.ownerUid == state.me) { onOpenExpense(e.id) } }
                            }
                        }
                    }
                }
            }
        }
    }
}
