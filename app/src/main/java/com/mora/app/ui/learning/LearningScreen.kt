package com.mora.app.ui.learning

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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
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
import com.mora.app.ai.Learner
import com.mora.app.ai.Progress
import com.mora.app.ai.topics
import com.mora.app.ui.Lang
import com.mora.app.ui.assistant.tr
import kotlinx.coroutines.launch

@Composable
fun LearningScreen(lang: Lang, onBack: () -> Unit) {
    val context = LocalContext.current
    val progress = remember { Progress(context) }
    val scope = rememberCoroutineScope()
    var topic by remember { mutableStateOf(topics[0]) }
    var content by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var lessons by remember { mutableStateOf(progress.lessons()) }
    var quizzes by remember { mutableStateOf(progress.quizzes()) }
    val outLang = tr(lang, "Malagasy", "French", "English")

    fun run(isQuiz: Boolean) {
        if (loading) return
        loading = true
        error = ""
        scope.launch {
            val r = if (isQuiz) Learner.quiz(topic, outLang) else Learner.lesson(topic, outLang)
            when (r) {
                is AiResult.Ok -> {
                    content = r.text.trim()
                    if (isQuiz) { progress.addQuiz(); quizzes = progress.quizzes() }
                    else { progress.addLesson(); lessons = progress.lessons() }
                }
                is AiResult.Fail -> error = tr(
                    lang, "Mila internet ny fianarana.",
                    "Internet requis pour apprendre.",
                    "Internet is required for learning."
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
        Text(tr(lang, "Fianarana", "Apprendre", "Learning"),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(
            tr(lang, "Lesona: $lessons · Quiz: $quizzes",
                "Leçons : $lessons · Quiz : $quizzes",
                "Lessons: $lessons · Quizzes: $quizzes"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)

        topics.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { t ->
                    val m = Modifier.weight(1f)
                    val label = tr(lang, t.mg, t.fr, t.en)
                    if (t.key == topic.key) Button(onClick = { topic = t }, modifier = m) { Text(label) }
                    else FilledTonalButton(onClick = { topic = t }, modifier = m) { Text(label) }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { run(false) }, enabled = !loading, modifier = Modifier.weight(1f)) {
                Text(if (loading) "…" else tr(lang, "Lesona", "Leçon", "Lesson"))
            }
            Button(onClick = { run(true) }, enabled = !loading, modifier = Modifier.weight(1f)) {
                Text(if (loading) "…" else "Quiz")
            }
        }
        if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
        if (content.isNotEmpty()) {
            Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp),
                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text(content, Modifier.padding(16.dp))
            }
        }
    }
}
