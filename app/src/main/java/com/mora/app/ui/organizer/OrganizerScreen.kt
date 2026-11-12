package com.mora.app.ui.organizer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.mora.app.data.MoneyEntity
import com.mora.app.data.MoraDatabase
import com.mora.app.data.TaskEntity
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val fmt = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.FRANCE))
private fun money(v: Double): String = fmt.format(v) + " Ar"

@Composable
fun OrganizerScreen(onBack: () -> Unit) {
    var tab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Hiverina") }
        Text(
            "Organizer",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Voatahiry ao amin'ny findainao, mandeha tsy mila internet.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Asa", "Vola sy trosa").forEachIndexed { i, label ->
                if (tab == i) Button(onClick = { tab = i }) { Text(label) }
                else FilledTonalButton(onClick = { tab = i }) { Text(label) }
            }
        }
        Spacer(Modifier.height(12.dp))

        if (tab == 0) TasksTab() else MoneyTab()
    }
}

@Composable
private fun TasksTab() {
    val context = LocalContext.current
    val dao = remember { MoraDatabase.get(context).taskDao() }
    val tasks by dao.all().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var newTitle by remember { mutableStateOf("") }

    fun add() {
        val t = newTitle.trim()
        if (t.isEmpty()) return
        scope.launch { dao.insert(TaskEntity(title = t)) }
        newTitle = ""
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newTitle,
            onValueChange = { newTitle = it },
            label = { Text("Asa vaovao") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        Button(onClick = { add() }) { Text("Ampio") }
    }
    Spacer(Modifier.height(12.dp))

    if (tasks.isEmpty()) {
        Text(
            "Mbola tsy misy asa.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(tasks, key = { it.id }) { task ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = task.done,
                        onCheckedChange = {
                            scope.launch { dao.update(task.copy(done = it)) }
                        }
                    )
                    Text(
                        task.title,
                        modifier = Modifier.weight(1f),
                        textDecoration = if (task.done) TextDecoration.LineThrough else null,
                        color = if (task.done) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { scope.launch { dao.delete(task.id) } }) {
                        Text("Fafao")
                    }
                }
            }
        }
    }
}

private val types = listOf(
    "INCOME" to "Fidiram-bola",
    "EXPENSE" to "Lany",
    "DEBT_OWED" to "Trosako",
    "DEBT_RECEIVABLE" to "Trosan'olona"
)

private fun typeLabel(t: String) = types.firstOrNull { it.first == t }?.second ?: t

@Composable
private fun MoneyTab() {
    val context = LocalContext.current
    val dao = remember { MoraDatabase.get(context).moneyDao() }
    val items by dao.all().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var type by remember { mutableStateOf("INCOME") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val income = items.filter { it.type == "INCOME" }.sumOf { it.amount }
    val expense = items.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val owed = items.filter { it.type == "DEBT_OWED" && !it.settled }.sumOf { it.amount }
    val receivable = items.filter { it.type == "DEBT_RECEIVABLE" && !it.settled }.sumOf { it.amount }

    fun add() {
        val a = amount.replace(',', '.').replace(" ", "").toDoubleOrNull() ?: return
        if (a <= 0) return
        scope.launch { dao.insert(MoneyEntity(type = type, amount = a, note = note.trim())) }
        amount = ""
        note = ""
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SummaryRow("Fidiram-bola", money(income))
                    SummaryRow("Lany", money(expense))
                    SummaryRow("Tavela (fidiram-bola − lany)", money(income - expense), bold = true)
                    SummaryRow("Trosako (tsy maintsy aloako)", money(owed))
                    SummaryRow("Trosan'olona (horaisiko)", money(receivable))
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                types.forEach { (key, label) ->
                    if (type == key) Button(
                        onClick = { type = key },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) { Text(label, style = MaterialTheme.typography.labelSmall) }
                    else FilledTonalButton(
                        onClick = { type = key },
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                    ) { Text(label, style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
        item {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Vola (Ar)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Fanamarihana / anaran'olona") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Button(onClick = { add() }) { Text("Ampio") }
            }
        }
        if (items.isEmpty()) {
            item {
                Text(
                    "Mbola tsy misy firaketana.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(items, key = { it.id }) { m ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${typeLabel(m.type)}: ${money(m.amount)}",
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (m.settled) TextDecoration.LineThrough else null
                        )
                        if (m.note.isNotEmpty()) {
                            Text(
                                m.note,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (m.type.startsWith("DEBT")) {
                        TextButton(
                            onClick = { scope.launch { dao.update(m.copy(settled = !m.settled)) } }
                        ) { Text(if (m.settled) "Averina" else "Voaloa") }
                    }
                    TextButton(onClick = { scope.launch { dao.delete(m.id) } }) { Text("Fafao") }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            value,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
