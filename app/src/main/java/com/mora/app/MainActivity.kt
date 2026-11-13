package com.mora.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mora.app.ui.Lang
import com.mora.app.ui.assistant.AssistantScreen
import com.mora.app.ui.calcul.CalcScreen
import com.mora.app.ui.home.HomeScreen
import com.mora.app.ui.organizer.OrganizerScreen
import com.mora.app.ui.settings.SettingsScreen
import com.mora.app.ui.stringsFor
import com.mora.app.ui.theme.MoraTheme
import com.mora.app.ui.translate.TranslateScreen
import com.mora.app.ui.writing.WritingScreen

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
                        "live" -> TranslateScreen(lang = lang, onBack = { screen = "home" })
                        "writing" -> WritingScreen(lang = lang, onBack = { screen = "home" })
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
