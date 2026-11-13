package com.mora.app.ui.assistant

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mora.app.ai.personalityByKey
import com.mora.app.data.ChatStore
import com.mora.app.ui.Lang
import kotlinx.coroutines.launch

@Composable
fun ChatListScreen(store: ChatStore, lang: Lang, onBack: () -> Unit, onOpen: (Long) -> Unit) {
    val chats by store.chats.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val newTitle = tr(lang, "Resaka vaovao", "Nouveau chat", "New chat")

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onBack) { Text(tr(lang, "← Hiverina", "← Retour", "← Back")) }
        Text(
            "Assistant",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { scope.launch { onOpen(store.newChat("namana", newTitle)) } },
            modifier = Modifier.fillMaxWidth()
        ) { Text("+ $newTitle") }
        Spacer(Modifier.height(12.dp))
        if (chats.isEmpty()) {
            Text(
                tr(lang, "Mbola tsy misy resaka.", "Aucune conversation.", "No chats yet."),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(chats, key = { it.id }) { c ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onOpen(c.id) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(c.title, fontWeight = FontWeight.SemiBold)
                            Text(
                                personalityByKey(c.persona).name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { scope.launch { store.delete(c.id) } }) {
                            Text(tr(lang, "Fafao", "Suppr.", "Delete"))
                        }
                    }
                }
            }
        }
    }
}
