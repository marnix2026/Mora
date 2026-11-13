package com.mora.app.ui.home

import com.mora.app.ui.Strings
import java.util.Calendar

data class Shortcut(
    val key: String,
    val icon: String,
    val title: (Strings) -> String,
    val subtitle: (Strings) -> String,
    val ready: Boolean
)

val shortcuts = listOf(
    Shortcut("assistant", "AI", { it.assistantTitle }, { it.assistantSub }, true),
    Shortcut("live", "TR", { it.liveTitle }, { it.liveSub }, true),
    Shortcut("calcul", "123", { it.calcTitle }, { it.calcSub }, true),
    Shortcut("organizer", "OK", { it.orgTitle }, { it.orgSub }, true),
    Shortcut("photo", "IMG", { it.photoTitle }, { it.photoSub }, false),
        Shortcut("document", "PDF", { it.docTitle }, { it.docSub }, false),
    Shortcut("writing", "AB", { "Writing" }, { "CV, taratasy, Facebook" }, true)
)

fun greeting(s: Strings): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> s.greetMorning
        hour < 18 -> s.greetAfternoon
        else -> s.greetEvening
    }
}
