package com.mora.app.ai

class WriteMode(val key: String, val mg: String, val fr: String, val en: String, val prompt: String)

val writeModes = listOf(
    WriteMode("cv", "CV", "CV", "CV",
        "Write a clear, professional CV from the details given. Use clean sections. Do not invent facts: if something is missing, put [to fill]."),
    WriteMode("letter", "Taratasy", "Lettre", "Letter",
        "Write a well-structured formal letter from the details given. Do not invent facts."),
    WriteMode("email", "Mailaka", "E-mail", "Email",
        "Write a clear, polite email with a subject line from the details given."),
    WriteMode("facebook", "Facebook", "Facebook", "Facebook",
        "Write an engaging Facebook post or caption from the details given. Keep it natural."),
    WriteMode("fix", "Ahitsio", "Corriger", "Fix",
        "Correct spelling, grammar and punctuation of the text. Keep the meaning and language. Return only the corrected text."),
    WriteMode("rewrite", "Averina soratana", "Réécrire", "Rewrite",
        "Rewrite the text to be clearer and better written. Keep the meaning and language. Return only the new text.")
)

object Writer {
    suspend fun write(mode: WriteMode, text: String, outLang: String): AiResult {
        val system = mode.prompt + " Write the result in $outLang. " +
            "You are an AI assistant; never invent personal data."
        return AiClient.chat(system, listOf(ChatMsg("user", text)))
    }
}
