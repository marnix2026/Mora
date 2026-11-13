package com.mora.app.ui.assistant

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mora.app.ai.AiClient
import com.mora.app.ai.AiResult
import com.mora.app.ai.ChatMsg
import com.mora.app.ai.buildSystemPrompt
import com.mora.app.ai.personalityByKey
import com.mora.app.data.ChatStore
import com.mora.app.ui.Lang
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(store: ChatStore, chatId: Long, lang: Lang, onBack: () -> Unit) {
    val chats by store.chats.collectAsState(initial = emptyList())
    val chat = chats.firstOrNull { it.id == chatId }
    val allMsgs by store.messages(chatId).collectAsState(initial = emptyList())
    val msgs = allMsgs.filter { it.persona == (chat?.persona ?: "namana") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var renaming by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }

    LaunchedEffect(msgs.size) {
        if (msgs.isNotEmpty()) listState.animateScrollToItem(msgs.size - 1)
    }
    if (chat == null) {
        TextButton(onClick = onBack) { Text("←") }
        return
    }

    fun send() {
        val t = input.trim()
        if (t.isEmpty() || loading) return
        input = ""
        loading = true
        error = ""
        scope.launch {
            if (allMsgs.isEmpty()) store.rename(chat, t.take(24))
            store.add(chat, "user", t, chat.persona)
            val hist = store.history(chatId)
                .filter { it.persona == chat.persona }
                .map { ChatMsg(it.role, it.text) }
            val p = personalityByKey(chat.persona)
            when (val r = AiClient.chat(buildSystemPrompt(p, p.name, lang.code), hist)) {
                is AiResult.Ok -> store.add(chat, "assistant", r.text, chat.persona)
                is AiResult.Fail -> error = tr(
                    lang, "Tsy mamaly ny AI. Hamarino ny internet.",
                    "L'IA ne répond pas. Vérifiez internet.",
                    "The AI is not responding. Check your internet."
                ) + " [" + r.detail + "]"
            }
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text(tr(lang, "← Resaka rehetra", "← Chats", "← Chats")) }
            TextButton(onClick = { title = chat.title; renaming = !renaming }) {
                Text(tr(lang, "Ovay anarana", "Renommer", "Rename"))
            }
        }
        if (renaming) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(title, { title = it }, Modifier.weight(1f), singleLine = true)
                Button(onClick = {
                    scope.launch { store.rename(chat, title.trim().ifEmpty { chat.title }) }
                    renaming = false
                }) { Text("OK") }
            }
        } else {
            Text(chat.title, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Text(personalityByKey(chat.persona).tone, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        PersonaRow(chat.persona) { k -> scope.launch { store.setPersona(chat, k) } }
        Spacer(Modifier.height(8.dp))

        LazyColumn(Modifier.weight(1f).fillMaxWidth(), listState,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(msgs, key = { it.id }) { m -> Bubble(m.text, m.role == "user") }
            if (loading) item { Text("…") }
            if (error.isNotEmpty()) item { Bubble(error, false, true) }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(input, { input = it }, Modifier.weight(1f),
                label = { Text(tr(lang, "Soraty ny hafatra", "Écrivez un message", "Type a message")) },
                maxLines = 4)
            Button(onClick = { send() }, enabled = !loading) {
                Text(tr(lang, "Alefa", "Envoyer", "Send"))
            }
        }
    }
}
