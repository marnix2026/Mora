package com.mora.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private fun colorFor(key: String): Color = when (key) {
    "namana" -> Color(0xFF5B3FD0)
    "olontiana" -> Color(0xFFD0508A)
    "zandry" -> Color(0xFFE08A1E)
    "zoky" -> Color(0xFF2E7D6B)
    "mpanoro" -> Color(0xFF3A6FB0)
    else -> Color(0xFF9C4DCC)
}

@Composable
fun Portrait(key: String, name: String, size: Dp = 64.dp) {
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(colorFor(key)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            name.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.45f).sp
        )
    }
}
