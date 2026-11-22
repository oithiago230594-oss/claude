package app.casal.financas.ui.expense

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import app.casal.financas.domain.Category
import app.casal.financas.domain.Cycles
import app.casal.financas.domain.Expense
import app.casal.financas.ui.FinanceState
import app.casal.financas.ui.components.MoneyField
import app.casal.financas.ui.components.personColor
import app.casal.financas.ui.home.nameFor
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseEditorScreen(
    state: FinanceState,
    existing: Expense?,
    onBack: () -> Unit,
    onSave: (owner: String, cents: Long, description: String, category: Category, date: LocalDate) -> Unit,
    onDelete: (String) -> Unit,
) {
    var cents by rememberSaveable { mutableLongStateOf(existing?.amountCents ?: 0L) }
    var owner by rememberSaveable { mutableStateOf(existing?.ownerUid ?: state.me) }
    var category by rememberSaveable { mutableStateOf(existing?.category ?: Category.MARKET) }
    var description by rememberSaveable { mutableStateOf(existing?.description ?: "") }
    var dateIso by rememberSaveable { mutableStateOf((existing?.date ?: LocalDate.now()).toString()) }
    var showDate by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val date = LocalDate.parse(dateIso)
    val members = state.summary.people

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Novo gasto" else "Editar gasto") },
                navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Voltar") } },
                actions = {
                    if (existing != null) IconButton({ confirmDelete = true }) { Icon(Icons.Rounded.Delete, "Excluir", tint = MaterialTheme.colorScheme.error) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = { onSave(owner, cents, description, category, date); onBack() },
                    enabled = cents > 0,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(16.dp).height(52.dp),
                ) { Text(if (existing == null) "Lançar gasto" else "Salvar alterações") }
            }
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            MoneyField(cents, { cents = it }, "Valor", accent = personColor(owner == state.me), large = true)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Quem gastou?", style = MaterialTheme.typography.labelLarge)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    members.forEachIndexed { i, p ->
                        SegmentedButton(
                            selected = owner == p.uid,
                            onClick = { owner = p.uid },
                            shape = SegmentedButtonDefaults.itemShape(i, members.size),
                            colors = SegmentedButtonDefaults.colors(activeContainerColor = personColor(p.isMe).copy(alpha = 0.18f), activeContentColor = personColor(p.isMe)),
                        ) { Text(p.name) }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Categoria", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Category.entries.forEach { c ->
                        FilterChip(
                            selected = category == c, onClick = { category = c },
                            label = { Text(c.label) },
                            leadingIcon = { Icon(c.icon, null, Modifier.size(18.dp), tint = c.color) },
                        )
                    }
                }
            }

            OutlinedTextField(
                description, { description = it }, label = { Text("Descrição (opcional)") }, singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
            )

            OutlinedButton({ showDate = true }, Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.medium) {
                Icon(Icons.Rounded.CalendarMonth, null)
                Spacer(Modifier.width(8.dp))
                val formatted = "%02d/%02d/%d".format(date.dayOfMonth, date.monthValue, date.year)
                val relative = Cycles.dayLabel(date, LocalDate.now())
                Text(if (relative == "Hoje" || relative == "Ontem") "$relative · $formatted" else formatted)
                Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDate) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton({
                    pickerState.selectedDateMillis?.let { dateIso = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString() }
                    showDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton({ showDate = false }) { Text("Cancelar") } },
        ) { DatePicker(pickerState) }
    }

    if (confirmDelete && existing != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Excluir gasto?") },
            text = { Text("Este gasto de ${nameFor(state, existing.ownerUid)} será removido nos dois celulares.") },
            confirmButton = { TextButton({ onDelete(existing.id); confirmDelete = false; onBack() }) { Text("Excluir", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton({ confirmDelete = false }) { Text("Cancelar") } },
        )
    }
}
