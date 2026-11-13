package com.mora.app.ui.memory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mora.app.ai.MemoryStore
import com.mora.app.ui.Lang
import com.mora.app.ui.assistant.tr

@Composable
fun MemoryScreen(lang: Lang, onBack: () -> Unit) {
    val store = remember { MemoryStore(LocalContext.current) }
    var items by remember { mutableStateOf(store.all()) }
    var enabled by remember { mutableStateOf(store.enabled) }
    var input by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf(-1) }

    fun save() {
        val t = input.trim()
        if (t.isEmpty()) return
        if (editing >= 0) store.update(editing, t) else store.add(t)
        editing = -1
        input = ""
        items = store.all()
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TextButton(onClick = onBack) { Text(tr(lang, "← Hiverina", "← Retour", "← Back")) }
        Text("Memory", style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(
            tr(lang, "Ny zavatra soratanao eto ihany no tadidin'ny AI. Aza asiana tahirin-kevitra miafina (teny miafina, kaonty banky).",
                "L'IA ne retient que ce que vous écrivez ici. Évitez les données sensibles (mots de passe, comptes bancaires).",
                "The AI only remembers what you write here. Avoid sensitive data (passwords, bank accounts)."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tr(lang, "Alefa ny Memory", "Mémoire activée", "Memory on"), Modifier.weight(1f))
            Switch(checked = enabled, onCheckedChange = { enabled = it; store.enabled = it })
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(input, { input = it }, Modifier.weight(1f),
                label = { Text(tr(lang, "Zavatra tadidiko", "À retenir", "To remember")) })
            Button(onClick = { save() }) {
                Text(if (editing >= 0) "OK" else tr(lang, "Ampio", "Ajouter", "Add"))
            }
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(items) { i, t ->
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(14.dp),
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(t, Modifier.weight(1f))
                        TextButton(onClick = { editing = i; input = t }) {
                            Text(tr(lang, "Ovay", "Modifier", "Edit"))
                        }
                        TextButton(onClick = { store.delete(i); items = store.all(); editing = -1 }) {
                            Text(tr(lang, "Fafao", "Suppr.", "Delete"))
                        }
                    }
                }
            }
        }
    }
}
