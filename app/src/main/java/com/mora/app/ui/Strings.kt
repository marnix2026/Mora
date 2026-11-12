package com.mora.app.ui

enum class Lang(val code: String, val label: String) {
    MG("mg", "Malagasy"),
    FR("fr", "Français"),
    EN("en", "English");

    companion object {
        fun from(code: String?): Lang = values().firstOrNull { it.code == code } ?: MG
    }
}

data class Strings(
    val tagline: String,
    val greetMorning: String,
    val greetAfternoon: String,
    val greetEvening: String,
    val companionNote: String,
    val features: String,
    val settings: String,
    val comingSoon: String,
    val back: String,
    val language: String,
    val theme: String,
    val themeSystem: String,
    val themeLight: String,
    val themeDark: String,
    val about: String,
    val aboutText: String,
    val privacy: String,
    val privacyText: String,
    val assistantTitle: String, val assistantSub: String,
    val liveTitle: String, val liveSub: String,
    val calcTitle: String, val calcSub: String,
    val orgTitle: String, val orgSub: String,
    val photoTitle: String, val photoSub: String,
    val docTitle: String, val docSub: String
)

private val mg = Strings(
    tagline = "Your Smart Everyday Companion",
    greetMorning = "Salama, mazotoa!",
    greetAfternoon = "Salama tsara!",
    greetEvening = "Manao ahoana, tsara vesatra!",
    companionNote = "Mpiara-miasa AI aho, tsy olombelona.",
    features = "Fiasa",
    settings = "Fanamboarana",
    comingSoon = "Mbola tsy vonona ity fiasa ity. Ho avy amin'ny dingana manaraka.",
    back = "← Hiverina",
    language = "Fiteny",
    theme = "Endrika",
    themeSystem = "Araka ny finday",
    themeLight = "Mazava",
    themeDark = "Maizina",
    about = "Momba ny app",
    aboutText = "MORA dikan-teny fanandramana 0.1. Ny kajy sy ny Organizer dia mandeha tsy mila internet. Ny fiasa AI dia mila internet rehefa vonona izy.",
    privacy = "Tsiambaratelo",
    privacyText = "Voatahiry ao amin'ny findainao ihany ny angonao. Tsy misy feo na sary alefa na voatahiry mangina.",
    assistantTitle = "Assistant", assistantSub = "Resaka amin'i MORA",
    liveTitle = "Live Translate", liveSub = "Fandikana feo",
    calcTitle = "Calcul", calcSub = "Kajy (offline)",
    orgTitle = "Organizer", orgSub = "Asa sy vola (offline)",
    photoTitle = "Photo AI", photoSub = "Sary sy OCR",
    docTitle = "Document", docSub = "PDF sy famintinana"
)

private val fr = Strings(
    tagline = "Your Smart Everyday Companion",
    greetMorning = "Bonjour !",
    greetAfternoon = "Bon après-midi !",
    greetEvening = "Bonsoir !",
    companionNote = "Je suis un compagnon IA, pas un humain.",
    features = "Fonctions",
    settings = "Paramètres",
    comingSoon = "Cette fonction n'est pas encore prête. Elle arrive dans une prochaine étape.",
    back = "← Retour",
    language = "Langue",
    theme = "Thème",
    themeSystem = "Selon le téléphone",
    themeLight = "Clair",
    themeDark = "Sombre",
    about = "À propos",
    aboutText = "MORA version d'essai 0.1. La calculatrice et l'Organizer fonctionnent sans internet. Les fonctions IA nécessiteront internet quand elles seront prêtes.",
    privacy = "Confidentialité",
    privacyText = "Vos données restent uniquement sur votre téléphone. Aucun son ni aucune photo n'est envoyé ou enregistré en secret.",
    assistantTitle = "Assistant", assistantSub = "Discuter avec MORA",
    liveTitle = "Live Translate", liveSub = "Traduction vocale",
    calcTitle = "Calcul", calcSub = "Calculs (hors ligne)",
    orgTitle = "Organizer", orgSub = "Tâches et argent (hors ligne)",
    photoTitle = "Photo AI", photoSub = "Photo et OCR",
    docTitle = "Document", docSub = "PDF et résumés"
)

private val en = Strings(
    tagline = "Your Smart Everyday Companion",
    greetMorning = "Good morning!",
    greetAfternoon = "Good afternoon!",
    greetEvening = "Good evening!",
    companionNote = "I'm an AI companion, not a human.",
    features = "Features",
    settings = "Settings",
    comingSoon = "This feature isn't ready yet. It's coming in a later step.",
    back = "← Back",
    language = "Language",
    theme = "Theme",
    themeSystem = "Follow phone",
    themeLight = "Light",
    themeDark = "Dark",
    about = "About",
    aboutText = "MORA test version 0.1. The calculator and Organizer work without internet. AI features will need internet once they are ready.",
    privacy = "Privacy",
    privacyText = "Your data stays on your phone only. No audio or photo is sent or saved in secret.",
    assistantTitle = "Assistant", assistantSub = "Chat with MORA",
    liveTitle = "Live Translate", liveSub = "Voice translation",
    calcTitle = "Calcul", calcSub = "Calculator (offline)",
    orgTitle = "Organizer", orgSub = "Tasks and money (offline)",
    photoTitle = "Photo AI", photoSub = "Photo and OCR",
    docTitle = "Document", docSub = "PDF and summaries"
)

fun stringsFor(lang: Lang): Strings = when (lang) {
    Lang.MG -> mg
    Lang.FR -> fr
    Lang.EN -> en
}
