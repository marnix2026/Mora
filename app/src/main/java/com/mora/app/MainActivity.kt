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
import com.mora.app.ui.assistant.AssistantScreen
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
                        "assistant" -> AssistantScreen(lang = lang, onBack = { screen = "home" })
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
    Shortcut("
