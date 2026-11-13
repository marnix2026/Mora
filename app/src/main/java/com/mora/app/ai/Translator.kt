package com.mora.app.ai

val langNames = listOf("Malagasy", "Français", "English")

object Translator {
    private fun system(from: String, to: String): String =
        "You are a translation engine. Translate the user's text from $from to $to. " +
            "Reply with ONLY the translation, no explanation, no quotes. " +
            "If the text is not clearly in $from, still translate it to $to. " +
            "Do not invent content that is not in the text."

    suspend fun translate(text: String, from: String, to: String): AiResult =
        AiClient.chat(system(from, to), listOf(ChatMsg("user", text)))
}
