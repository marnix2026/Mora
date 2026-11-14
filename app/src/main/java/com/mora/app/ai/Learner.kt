package com.mora.app.ai

import android.content.Context

class Topic(val key: String, val mg: String, val fr: String, val en: String, val subject: String)

val topics = listOf(
    Topic("french", "Frantsay", "Français", "French", "French language for a beginner"),
    Topic("english", "Anglisy", "Anglais", "English", "English language for a beginner"),
    Topic("computer", "Informatika", "Informatique", "Computer basics", "basic computer skills for a beginner"),
    Topic("general", "Fahalalana", "Culture générale", "General knowledge", "general knowledge")
)

object Learner {
    private fun base(lang: String) =
        "You are a patient teacher for a complete beginner. Write in $lang. " +
            "Keep it short, simple and accurate. If unsure, say so; never invent facts. "

    suspend fun lesson(t: Topic, lang: String): AiResult =
        AiClient.chat(
            base(lang) + "Give ONE short lesson (max 150 words) about " + t.subject +
                ", with 2 simple examples. Choose a new sub-topic each time.",
            listOf(ChatMsg("user", "Teach me something new."))
        )

    suspend fun quiz(t: Topic, lang: String): AiResult =
        AiClient.chat(
            base(lang) + "Create 3 multiple-choice questions (A, B, C) about " + t.subject +
                ". Format: number the questions, list options, and put all answers at the very end " +
                "after the line ANSWERS:",
            listOf(ChatMsg("user", "Give me a quiz."))
        )
}

class Progress(context: Context) {
    private val p = context.getSharedPreferences("mora_learning", Context.MODE_PRIVATE)
    fun lessons(): Int = p.getInt("lessons", 0)
    fun quizzes(): Int = p.getInt("quizzes", 0)
    fun addLesson() { p.edit().putInt("lessons", lessons() + 1).apply() }
    fun addQuiz() { p.edit().putInt("quizzes", quizzes() + 1).apply() }
}
