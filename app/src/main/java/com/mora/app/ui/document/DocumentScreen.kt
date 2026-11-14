package com.mora.app.ui.document

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.mora.app.ai.AiClient
import com.mora.app.ai.AiResult
import com.mora.app.ai.ChatMsg
import com.mora.app.ai.ImageData
import com.mora.app.ui.Lang
import com.mora.app.ui.assistant.tr
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val MAX_BYTES = 7 * 1024 * 1024

private fun nameOf(c: Context, uri: Uri): String =
    c.contentResolver.query(uri, null, null, null, null)?.use {
        val i = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (i >= 0 && it.moveToFirst()) it.getString(i) else null
    } ?: "document.pdf"

@Composable
fun DocumentScreen(lang: Lang, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fileName by remember { mutableStateOf("") }
    var b64 by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val outLang = tr(lang, "Malagasy", "French", "English")

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                answer = ""
                error = ""
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }
                if (bytes == null) {
                    error = tr(lang, "Tsy afaka namaky ny rakitra.", "Lecture impossible.", "Could not read the file.")
                } else if (bytes.size > MAX_BYTES) {
                    b64 = ""
                    fileName = ""
                    error = tr(lang, "Lehibe loatra ny PDF (mihoatra ny 7 MB).",
                        "PDF trop volumineux (plus de 7 Mo).", "PDF too large (over 7 MB).")
                } else {
                    fileName = nameOf(context, uri)
                    b64 = withContext(Dispatchers.Default) { Base64.encodeToString(bytes, Base64.NO_WRAP) }
                }
            }
        }
    }

    fun ask(q: String) {
        if (b64.isEmpty() || loading) return
        loading = true
        error = ""
        val system = "You read the attached PDF. Reply in $outLang. Answer ONLY from the document. " +
            "If the answer is not in the document, say clearly that it is not there. Never invent information."
        scope.launch {
            when (val r = AiClient.chat(system, listOf(ChatMsg("user", q)), ImageData("application/pdf", b64))) {
                is AiResult.Ok -> answer = r.text.trim()
                is AiResult.Fail -> error = tr(
                    lang, "Tsy afaka namaky ny PDF. Mila internet.",
                    "Lecture du PDF impossible. Internet requis.",
                    "Could not read the PDF. Internet is required."
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
        Text("Document", style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(
            tr(lang, "Ny PDF voafidy dia alefa any amin'ny serivisy AI. Hatramin'ny 7 MB. Aza misafidy PDF miafina.",
                "Le PDF choisi est envoyé au service IA. Maximum 7 Mo. Évitez les PDF sensibles.",
                "The chosen PDF is sent to the AI service. Max 7 MB. Avoid sensitive PDFs."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = { picker.launch("application/pdf") }, modifier = Modifier.fillMaxWidth()) {
            Text(tr(lang, "Misafidy PDF", "Choisir un PDF", "Choose a PDF"))
        }
        if (fileName.isNotEmpty()) {
            Text(fileName, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { ask("Summarize this document clearly in a few short points.") },
                    enabled = !loading, modifier = Modifier.weight(1f)) {
                    Text(if (loading) "…" else tr(lang, "Famintinana", "Résumé", "Summary"))
                }
            }
            OutlinedTextField(question, { question = it }, Modifier.fillMaxWidth(),
                label = { Text(tr(lang, "Fanontaniana momba ny PDF", "Question sur le PDF", "Question about the PDF")) })
            Button(onClick = { if (question.isNotBlank()) ask(question) },
                enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                Text(if (loading) "…" else tr(lang, "Anontanio", "Demander", "Ask"))
            }
        }
        if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        if (answer.isNotEmpty()) {
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp),
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text(answer)
                    Row {
                        TextButton(onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("MORA", answer))
                            Toast.makeText(context, "OK", Toast.LENGTH_SHORT).show()
                        }) { Text(tr(lang, "Kopio", "Copier", "Copy")) }
                        TextButton(onClick = {
                            val i = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, answer)
                            }
                            context.startActivity(Intent.createChooser(i, "MORA"))
                        }) { Text(tr(lang, "Zarao", "Partager", "Share")) }
                    }
                }
            }
        }
    }
}
