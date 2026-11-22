package app.casal.financas.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.casal.financas.domain.Expense
import app.casal.financas.domain.Money
import app.casal.financas.domain.PersonSummary
import app.casal.financas.ui.SyncStatus
import app.casal.financas.ui.theme.PersonColors

fun personColor(isMe: Boolean) = if (isMe) PersonColors.me else PersonColors.partner

/** Barra de progresso arredondada e animada. */
@Composable
fun RoundedProgress(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    track: Color = MaterialTheme.colorScheme.surfaceVariant,
) {
    val f by animateFloatAsState(fraction.coerceIn(0f, 1f), label = "progress")
    Box(modifier.fillMaxWidth().height(height).clip(CircleShape).background(track)) {
        Box(Modifier.fillMaxWidth(f).fillMaxHeight().clip(CircleShape).background(color))
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        action?.invoke()
    }
}

@Composable
fun PersonCard(p: PersonSummary, modifier: Modifier = Modifier) {
    val color = personColor(p.isMe)
    Card(
        modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(8.dp))
                Text(p.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                Money.format(p.remaining),
                style = MaterialTheme.typography.titleLarge,
                color = if (p.over) PersonColors.danger else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            RoundedProgress(p.fraction, if (p.over) PersonColors.danger else color, height = 8.dp)
            Text(
                "${Money.format(p.spent)} de ${Money.format(p.budget)}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

@Composable
fun ExpenseRow(expense: Expense, ownerName: String, ownerIsMe: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(44.dp).clip(CircleShape).background(expense.category.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) { Icon(expense.category.icon, null, tint = expense.category.color) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                expense.description.ifBlank { expense.category.label },
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, maxLines = 1,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(personColor(ownerIsMe)))
                Spacer(Modifier.width(6.dp))
                Text(
                    "$ownerName · ${expense.category.label}",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1,
                )
            }
        }
        Text("− " + Money.format(expense.amountCents), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SyncChip(status: SyncStatus, modifier: Modifier = Modifier) {
    val (icon, label, color) = when (status) {
        SyncStatus.SYNCED -> Triple(Icons.Rounded.CloudDone, "Sincronizado", PersonColors.ok)
        SyncStatus.SENDING -> Triple(Icons.Rounded.CloudSync, "Enviando…", MaterialTheme.colorScheme.primary)
        SyncStatus.OFFLINE -> Triple(Icons.Rounded.CloudOff, "Offline", MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Row(
        modifier.clip(CircleShape).background(color.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
fun EmptyState(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp)) }
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Campo de valor com máscara: o usuário digita só números e o campo vai preenchendo os centavos. */
@Composable
fun MoneyField(
    cents: Long,
    onChange: (Long) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    large: Boolean = false,
) {
    val text = Money.plain(cents)
    OutlinedTextField(
        value = TextFieldValue(text, TextRange(text.length)),
        onValueChange = { onChange(Money.centsFromDigits(it.text)) },
        label = { Text(label) },
        prefix = { Text("R$ ") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = if (large) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = accent, focusedLabelColor = accent),
        modifier = modifier.fillMaxWidth(),
    )
}
