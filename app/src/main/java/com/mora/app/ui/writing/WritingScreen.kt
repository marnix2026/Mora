package com.mora.app.ui.writing

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mora.app.ai.AiResult
import com.mora.app.ai.WriteMode
import com.mora.app.ai.Writer
import com.mora.app.ai.langNames
import com.mora.app.ai.writeModes
import com.mora.app.ui.Lang
import com.mora.app.ui.assistant.tr
import kotlinx.coroutines.launch

private fun label(m: WriteMode, lang: Lang) = tr(lang, m.mg, m.fr, m.en)

@Composable
fun WritingScreen(lang: Lang, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf(writeModes[0]) }
    var outLang by remember { mutableStateOf(0) }
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
            when (val r = Writer.write(mode, t, langNames[outLang])) {
                is AiResult.Ok -> output = r.text.trim()
                is AiResult.Fail -> error = tr(
                    lang, "Tsy afaka nanoratra. Mila internet.",
                    "Impossible d'écrire. Internet requis.",
                    "Could not write. Internet is required."
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
        Text(tr(lang, "Fanoratana", "Écriture", "Writing"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(writeModes) { m ->
                if (m.key == mode.key) Button(onClick = { mode = m }) { Text(label(m, lang)) }
                else FilledTonalButton(onClick = { mode = m }) { Text(label(m, lang)) }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            langNames.forEachIndexed { i, n ->
                if (i == outLang) Button(onClick = { outLang = i }) { Text(n) }
                else FilledTonalButton(onClick = { outLang = i }) { Text(n) }
            }
        }
        OutlinedTextField(input, { input = it }, Modifier.fillMaxWidth().height(150.dp),
            label = { Text(tr(lang, "Ny antsipiriany na soratra", "Détails ou texte", "Details or text")) })
        Button(onClick = { go() }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (loading) "…" else tr(lang, "Soraty", "Écrire", "Write"))
        }
        if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        if (output.isNotEmpty()) {
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp),
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text(output)
                    Row {
                        TextButton(onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("MORA", output))
                            Toast.makeText(context, "OK", Toast.LENGTH_SHORT).show()
                        }) { Text(tr(lang, "Kopio", "Copier", "Copy")) }
                        TextButton(onClick = {
                            val i = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, output)
                            }
                            context.startActivity(Intent.createChooser(i, "MORA"))
                        }) { Text(tr(lang, "Zarao", "Partager", "Share")) }
                    }
                }
            }
        }
    }
}
