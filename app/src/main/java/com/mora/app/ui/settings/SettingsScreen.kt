package com.mora.app.ui.settings

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mora.app.ui.Lang
import com.mora.app.ui.Strings

@Composable
fun SettingsScreen(
    strings: Strings,
    lang: Lang,
    themeMode: String,
    onLang: (Lang) -> Unit,
    onTheme: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        TextButton(onClick = onBack) { Text(strings.back) }
        Text(
            strings.settings,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(20.dp))

        Text(strings.language, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Lang.values().forEach { l ->
                val m = Modifier.weight(1f)
                if (l == lang) Button(onClick = { onLang(l) }, modifier = m) { Text(l.label) }
                else FilledTonalButton(onClick = { onLang(l) }, modifier = m) { Text(l.label) }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(strings.theme, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "system" to strings.themeSystem,
                "light" to strings.themeLight,
                "dark" to strings.themeDark
            ).forEach { (key, label) ->
                val m = Modifier.fillMaxWidth()
                if (themeMode == key) Button(onClick = { onTheme(key) }, modifier = m) { Text(label) }
                else FilledTonalButton(onClick = { onTheme(key) }, modifier = m) { Text(label) }
            }
        }

        Spacer(Modifier.height(24.dp))
        InfoCard(strings.privacy, strings.privacyText)
        Spacer(Modifier.height(12.dp))
        InfoCard(strings.about, strings.aboutText)
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(
                body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
