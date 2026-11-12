package com.mora.app

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.mora.app.ui.Lang
import com.mora.app.ui.Strings
import com.mora.app.ui.calcul.CalcScreen
import com.mora.app.ui.organizer.OrganizerScreen
import com.mora.app.ui.settings.SettingsScreen
import com.mora.app.ui.stringsFor
import com.mora.app.ui.theme.MoraTheme
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("mora_prefs", Context.MODE_PRIVATE)
        setContent {
            var lang by remember { mutableStateOf(Lang.from(prefs.getString("lang", "mg"))) }
            var themeMode by remember { mutableStateOf(prefs.getString("theme", "system") ?: "system") }
            val strings = stringsFor(lang)
            val dark = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            MoraTheme(darkTheme = dark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var screen by remember { mutableStateOf("home") }

                    BackHandler(enabled = screen != "home") { screen = "home" }

                    when (screen) {
                        "calcul" -> CalcScreen(onBack = { screen = "home" })
                        "organizer" -> OrganizerScreen(onBack = { screen = "home" })
                        "settings" -> SettingsScreen(
                            strings = strings,
                            lang = lang,
                            themeMode = themeMode,
                            onLang = {
                                lang = it
                                prefs.edit().putString("lang", it.code).apply()
                            },
                            onTheme = {
                                themeMode = it
                                prefs.edit().putString("theme", it).apply()
                            },
                            onBack = { screen = "home" }
                        )
                        else -> HomeScreen(strings = strings, onOpen = { screen = it })
                    }
                }
            }
        }
    }
}

private data class Shortcut(
    val key: String,
    val emoji: String,
    val title: (Strings) -> String,
    val subtitle: (Strings) -> String,
    val ready: Boolean
)

private val shortcuts = listOf(
    Shortcut("assistant", "💬", { it.assistantTitle }, { it.assistantSub }, false),
    Shortcut("live", "🌍", { it.liveTitle }, { it.liveSub }, false),
    Shortcut("calcul", "🧮", { it.calcTitle }, { it.calcSub }, true),
    Shortcut("organizer", "📋", { it.orgTitle }, { it.orgSub }, true),
    Shortcut("photo", "📷", { it.photoTitle }, { it.photoSub }, false),
    Shortcut("document", "📄", { it.docTitle }, { it.docSub }, false)
)

private fun greeting(s: Strings): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> s.greetMorning
        hour < 18 -> s.greetAfternoon
        else -> s.greetEvening
    }
}

@Composable
fun HomeScreen(strings: Strings, onOpen: (String) -> Unit) {
    val context = LocalContext.current
    val soon = {
        Toast.makeText(context, strings.comingSoon, Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "MORA",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = strings.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = { onOpen("settings") }) { Text("⚙ ${strings.settings}") }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = greeting(strings), style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { soon() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "N",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Namana",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = strings.companionNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = strings.features,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        shortcuts.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(112.dp)
                            .clickable { if (item.ready) onOpen(item.key) else soon() },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = item.emoji, style = MaterialTheme.typography.headlineSmall)
                            Column {
                                Text(
                                    text = item.title(strings),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = item.subtitle(strings),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
