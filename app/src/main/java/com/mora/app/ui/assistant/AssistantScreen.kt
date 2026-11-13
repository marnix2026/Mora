package com.mora.app.ui.assistant

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.mora.app.data.ChatStore
import com.mora.app.ui.Lang

fun tr(lang: Lang, mg: String, fr: String, en: String): String =
    when (lang) {
        Lang.MG -> mg
        Lang.FR -> fr
        Lang.EN -> en
    }

@Composable
fun AssistantScreen(lang: Lang, onBack: () -> Unit) {
    val context = LocalContext.current
    val store = remember { ChatStore(context) }
    var openId by remember { mutableStateOf<Long?>(null) }
    val id = openId
    if (id == null) {
        ChatListScreen(store, lang, onBack) { openId = it }
    } else {
        ChatScreen(store, id, lang) { openId = null }
    }
}
