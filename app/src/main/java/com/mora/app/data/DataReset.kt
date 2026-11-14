package com.mora.app.data

import android.content.Context

object DataReset {
    fun wipeAll(context: Context) {
        MoraDatabase.get(context).clearAllTables()
        listOf("mora_memory", "mora_learning").forEach {
            context.getSharedPreferences(it, Context.MODE_PRIVATE).edit().clear().apply()
        }
    }
}
