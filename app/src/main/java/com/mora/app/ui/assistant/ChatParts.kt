package com.mora.app.ui.assistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mora.app.ai.personalities

@Composable
fun PersonaRow(current: String, onPick: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        items(personalities) { p ->
            if (p.key == current) Button(onClick = { onPick(p.key) }) { Text(p.name) }
            else FilledTonalButton(onClick = { onPick(p.key) }) { Text(p.name) }
        }
    }
}

@Composable
fun Bubble(text: String, mine: Boolean, isError: Boolean = false) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isError -> MaterialTheme.colorScheme.errorContainer
                    mine -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Text(text, modifier = Modifier.padding(12.dp))
        }
    }
}
