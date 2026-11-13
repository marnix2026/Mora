package com.mora.app.ui.translate

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mora.app.ai.AiResult
import com.mora.app.ai.Translator
import com.mora.app.ai.langNames
import com.mora.app.ui.Lang
import com.mora.app.ui.assistant.tr
import kotlinx.coroutines.launch

@Composable
fun TranslateScreen(lang: Lang, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var from by remember { mutableStateOf(1) }
    var to by remember { mutableStateOf(2) }
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    fun go() {
        val t = input.trim()
        if (t.isEmpty() || loading) return
        loading = true
        error = ""
        scope.launch {
            when (val r = Translator.translate(t, langNames[from], langNames[to])) {
                is AiResult.Ok -> output = r.text.trim()
                is AiResult.Fail -> error = tr(
                    lang, "Tsy afaka nandika. Mila internet ny fandikana.",
                    "Traduction impossible. Internet requis.",
                    "Could not translate. Internet is required."
                ) + " [" + r.detail + "]"
            }
            loading = false
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TextButton(onClick = onBack) { Text(tr(lang, "← Hiverina", "← Retour", "← Back")) }
        Text("Translate", style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        LangPicker(tr(lang, "Avy amin'ny", "De", "From"), from) { from = it }
        LangPicker(tr(lang, "Ho amin'ny", "Vers", "To"), to) { to = it }
        TextButton(onClick = { val a = from; from = to; to = a }) {
            Text(tr(lang, "⇅ Ovay toerana", "⇅ Inverser", "⇅ Swap"))
        }

        OutlinedTextField(input, { input = it }, Modifier.fillMaxWidth().height(140.dp),
            label = { Text(tr(lang, "Soratra", "Texte", "Text")) })
        Button(onClick = { go() }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (loading) "…" else tr(lang, "Dikao", "Traduire", "Translate"))
        }
        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        if (output.isNotEmpty()) {
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp),
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text(output)
                    TextButton(onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("MORA", output))
                        Toast.makeText(context, "OK", Toast.LENGTH_SHORT).show()
                    }) { Text(tr(lang, "Kopio", "Copier", "Copy")) }
                }
            }
        }
    }
}

@Composable
private fun LangPicker(label: String, selected: Int, onPick: (Int) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            langNames.forEachIndexed { i, n ->
                if (i == selected) Button(onClick = { onPick(i) }) { Text(n) }
                else FilledTonalButton(onClick = { onPick(i) }) { Text(n) }
            }
        }
    }
}
