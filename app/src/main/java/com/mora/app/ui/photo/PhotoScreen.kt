package com.mora.app.ui.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mora.app.ai.AiClient
import com.mora.app.ai.AiResult
import com.mora.app.ai.ChatMsg
import com.mora.app.ai.ImageData
import com.mora.app.ui.Lang
import com.mora.app.ui.assistant.tr
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun loadScaled(context: Context, uri: Uri): Bitmap? {
    val bmp = context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it)
    } ?: return null
    val max = 1280
    val scale = max.toFloat() / maxOf(bmp.width, bmp.height)
    if (scale >= 1f) return bmp
    return Bitmap.createScaledBitmap(
        bmp, (bmp.width * scale).toInt(), (bmp.height * scale).toInt(), true
    )
}

private fun toBase64(bmp: Bitmap): String {
    val out = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.JPEG, 80, out)
    return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
}

@Composable
fun PhotoScreen(lang: Lang, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val outLang = tr(lang, "Malagasy", "French", "English")

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                bitmap = withContext(Dispatchers.IO) { loadScaled(context, uri) }
                answer = ""
                error = ""
            }
        }
    }

    fun ask(q: String) {
        val b = bitmap ?: return
        if (loading) return
        loading = true
        error = ""
        scope.launch {
            val b64 = withContext(Dispatchers.Default) { toBase64(b) }
            val system = "You analyze the photo the user sends. Reply in $outLang. " +
                "Only describe what is really visible; if unsure or unreadable, say so. " +
                "Never invent text that is not in the image."
            when (val r = AiClient.chat(
                system, listOf(ChatMsg("user", q)), ImageData("image/jpeg", b64)
            )) {
                is AiResult.Ok -> answer = r.text.trim()
                is AiResult.Fail -> error = tr(
                    lang, "Tsy afaka nandinika ny sary. Mila internet.",
                    "Analyse impossible. Internet requis.",
                    "Could not analyze the photo. Internet is required."
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
        Text("Photo AI", style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(
            tr(lang, "Ny sary voafidy dia alefa any amin'ny serivisy AI mba hodinihina. Aza misafidy sary miafina.",
                "La photo choisie est envoyée au service IA pour analyse. Évitez les photos sensibles.",
                "The chosen photo is sent to the AI service for analysis. Avoid sensitive photos."),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text(tr(lang, "Misafidy sary", "Choisir une photo", "Choose a photo"))
        }
        bitmap?.let {
            Image(it.asImageBitmap(), null, Modifier.fillMaxWidth().heightIn(max = 260.dp))
            OutlinedTextField(question, { question = it }, Modifier.fillMaxWidth(),
                label = { Text(tr(lang, "Fanontaniana momba ny sary", "Question sur la photo", "Question about the photo")) })
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { ask(question.ifBlank { "Describe this photo." }) },
                    enabled = !loading, modifier = Modifier.weight(1f)
                ) { Text(if (loading) "…" else tr(lang, "Anontanio", "Demander", "Ask")) }
                Button(
                    onClick = { ask("Extract all visible text exactly as written (OCR), then briefly explain it.") },
                    enabled = !loading, modifier = Modifier.weight(1f)
                ) { Text(if (loading) "…" else "OCR") }
            }
        }
        if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        if (answer.isNotEmpty()) {
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp),
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text(answer, Modifier.padding(16.dp))
            }
        }
    }
}
