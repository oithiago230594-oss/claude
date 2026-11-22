package app.casal.financas.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.casal.financas.domain.Cycles
import app.casal.financas.domain.Money
import app.casal.financas.ui.FinanceState
import app.casal.financas.ui.components.*
import app.casal.financas.ui.theme.PersonColors

@Composable
fun HomeScreen(
    state: FinanceState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenExpense: (String) -> Unit,
    onSeeAll: () -> Unit,
    onEditBudget: () -> Unit,
    onShareInvite: () -> Unit,
) {
    val s = state.summary
    val h = state.household
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddExpense, icon = { Icon(Icons.Rounded.Add, null) }, text = { Text("Novo gasto") })
        },
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Finanças do Casal", style = MaterialTheme.typography.titleLarge)
                        Text("Atualizado em tempo real", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    SyncChip(state.sync)
                }
            }
            item { CycleSwitcher(state, onPrev, onNext, onToday) }
            item { HeroCard(state) }

            if (!h.isComplete) item { InviteBanner(h.inviteCode, onShareInvite) }

            if (!s.hasBudget) item {
                ElevatedCard(onClick = onEditBudget, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Defina o valor do ciclo", style = MaterialTheme.typography.titleMedium)
                            Text("Quanto cada um pode gastar neste período.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null)
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    s.people.forEach { PersonCard(it, Modifier.weight(1f).fillMaxHeight()) }
                    if (s.people.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            if (s.byCategory.isNotEmpty()) item { CategoryCard(state) }

            item {
                SectionTitle("Últimos gastos") {
                    if (state.expenses.isNotEmpty()) TextButton(onSeeAll) { Text("Ver todos") }
                }
            }
            if (state.expenses.isEmpty()) {
                item {
                    EmptyState(Icons.Rounded.Receipt, "Nenhum gasto ainda", "Toque em \"Novo gasto\" para lançar o primeiro.")
                }
            } else {
                item {
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                            state.expenses.take(5).forEach { e ->
                                ExpenseRow(e, nameFor(state, e.ownerUid), e.ownerUid == state.me) { onOpenExpense(e.id) }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun nameFor(state: FinanceState, uid: String): String =
    if (uid == state.me) "Você" else state.household.nameOf(uid)

@Composable
fun CycleSwitcher(state: FinanceState, onPrev: () -> Unit, onNext: () -> Unit, onToday: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(MaterialTheme.colorScheme.surface).padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onPrev) { Icon(Icons.Rounded.ChevronLeft, "Ciclo anterior") }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(Cycles.title(state.cycleId), style = MaterialTheme.typography.titleMedium)
            Text(Cycles.rangeLabel(state.cycleId, state.household.cycleStartDay), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!state.isCurrentCycle) TextButton(onToday) { Text("Hoje") }
        IconButton(onNext) { Icon(Icons.Rounded.ChevronRight, "Próximo ciclo") }
    }
}

@Composable
private fun HeroCard(state: FinanceState) {
    val s = state.summary
    val tint = if (s.over) PersonColors.danger else MaterialTheme.colorScheme.primary
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (s.over) "Estourou o orçamento" else "Ainda podem gastar", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f))
            Text(Money.format(s.remaining), style = MaterialTheme.typography.headlineLarge, color = if (s.over) PersonColors.danger else MaterialTheme.colorScheme.onPrimaryContainer)
            RoundedProgress(s.fraction, tint, height = 12.dp, track = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
            Row(Modifier.fillMaxWidth()) {
                Text("Gasto ${Money.format(s.totalSpent)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.weight(1f))
                Text("Total ${Money.format(s.totalBudget)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            if (state.isCurrentCycle && s.dailyAllowance != null) {
                Row(
                    Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)).padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Today, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.width(6.dp))
                    Text("≈ ${Money.format(s.dailyAllowance)} por dia até o fim do ciclo", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun InviteBanner(code: String, onShare: () -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.PersonAdd, null, tint = MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Convide seu par", style = MaterialTheme.typography.titleMedium)
                Text("Código: $code", style = MaterialTheme.typography.bodyMedium)
            }
            FilledTonalButton(onShare) { Text("Enviar") }
        }
    }
}

@Composable
private fun CategoryCard(state: FinanceState) {
    val total = state.summary.totalSpent.coerceAtLeast(1)
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Para onde foi o dinheiro", style = MaterialTheme.typography.titleMedium)
            state.summary.byCategory.take(5).forEach { (cat, cents) ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(cat.icon, null, Modifier.size(18.dp), tint = cat.color)
                        Spacer(Modifier.width(8.dp))
                        Text(cat.label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        Text(Money.format(cents), style = MaterialTheme.typography.labelLarge)
                    }
                    RoundedProgress(cents.toFloat() / total, cat.color, height = 6.dp)
                }
            }
        }
    }
}
