package com.mora.app.ui.assistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.mora.app.ai.personalities
import com.mora.app.ai.personalityByKey
import com.mora.app.ui.Lang
import kotlinx.coroutines.launch

private class Line(val role: String, val text: String, val isError: Boolean = false)

@Composable
fun AssistantScreen(lang: Lang, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val lines = remember { mutableStateListOf<Line>() }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var personaKey by remember { mutableStateOf("namana") }
    val persona = personalityByKey(personaKey)
    val listState = rememberLazyListState()

    val errNetwork = when (lang) {
        Lang.FR -> "Pas de connexion internet. L'assistant IA a besoin d'internet. Vos fonctions hors ligne restent disponibles."
        Lang.EN -> "No internet connection. The AI assistant needs internet. Your offline features still work."
        Lang.MG -> "Tsy misy internet. Mila internet ny Assistant AI. Mbola mandeha ny fiasa offline."
    }
    val errServer = when (lang) {
        Lang.FR -> "Le service IA ne répond pas pour le moment. Réessayez plus tard."
        Lang.EN -> "The AI service is not responding right now. Please try again later."
        Lang.MG -> "Tsy mamaly ny serivisy AI amin'izao. Andramo indray any aoriana."
    }
    val hint = when (lang) {
        Lang.FR -> "Écrivez un message"
        Lang.EN -> "Type a message"
        Lang.MG -> "Soraty ny hafatra"
    }
    val send = when (lang) {
        Lang.FR -> "Envoyer"
        Lang.EN -> "Send"
        Lang.MG -> "Alefa"
    }
    val back = when (lang) {
        Lang.FR -> "← Retour"
        Lang.EN -> "← Back"
        Lang.MG -> "← Hiverina"
    }
    val clear = when (lang) {
        Lang.FR -> "Nouveau chat"
        Lang.EN -> "New chat"
        Lang.MG -> "Resaka vaovao"
    }
    val aiNote = when (lang) {
        Lang.FR -> "Je suis une IA, pas un humain. Je peux me tromper."
        Lang.EN -> "I'm an AI, not a human. I can make mistakes."
        Lang.MG -> "AI aho, tsy olombelona. Mety hiala tsy ho tsara indraindray."
    }

    LaunchedEffect(lines.size) {
        if (lines.isNotEmpty()) listState.animateScrollToItem(lines.size - 1)
    }

    fun sendMessage() {
        val t = input.trim()
        if (t.isEmpty() || loading) return
        lines.add(Line("user", t))
        input = ""
        loading = true
        val history = lines.filter { !it.isError }.map { ChatMsg(it.role, it.text) }
        val system = buildSystemPrompt(persona, persona.name, lang.code)
        scope.launch {
            when (val r = AiClient.chat(system, history)) {
                is AiResult.Ok -> lines.add(Line("assistant", r.text))
                is AiResult.Fail -> lines.add(
                                        Line("assistant", (if (r.kind == "network") errNetwork else errServer) + "\n[" + r.detail + "]", true)
                )
            }
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) { Text(back) }
            TextButton(onClick = { lines.clear() }) { Text(clear) }
        }
        Text(
            "Assistant · ${persona.name}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            persona.tone,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(personalities) { p ->
                if (p.key == personaKey) Button(onClick = { personaKey = p.key }) { Text(p.name) }
                else FilledTonalButton(onClick = { personaKey = p.key }) { Text(p.name) }
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (lines.isEmpty()) {
                item {
                    Text(aiNote, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(lines) { l ->
                val mine = l.role == "user"
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                l.isError -> MaterialTheme.colorScheme.errorContainer
                                mine -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Text(l.text, modifier = Modifier.padding(12.dp))
                    }
                }
            }
            if (loading) {
                item { Text("…", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text(hint) },
                modifier = Modifier.weight(1f),
                maxLines = 4
            )
            Button(onClick = { sendMessage() }, enabled = !loading) { Text(send) }
        }
    }
}
