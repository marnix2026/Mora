package com.mora.app.ui.home

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mora.app.ui.Strings

@Composable
fun HomeScreen(strings: Strings, onOpen: (String) -> Unit) {
    val context = LocalContext.current
    val soon = {
        Toast.makeText(context, strings.comingSoon, Toast.LENGTH_SHORT).show()
    }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Column {
                Text(
                    "MORA",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    strings.tagline,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
                        TextButton(onClick = { onOpen("settings") }) { Text("⚙", style = MaterialTheme.typography.titleLarge) }
        }
        Spacer(Modifier.height(24.dp))
        Text(greeting(strings), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
                CompanionCard(strings, "namana") { onOpen("assistant") }
        Spacer(Modifier.height(24.dp))
        Text(
            strings.features,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        shortcuts.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item ->
                    ShortcutCard(item, strings, Modifier.weight(1f)) {
                        if (item.ready) onOpen(item.key) else soon()
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
