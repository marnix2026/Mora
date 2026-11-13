package com.mora.app.ai

import android.content.Context
import org.json.JSONArray

class MemoryStore(context: Context) {
    private val prefs = context.getSharedPreferences("mora_memory", Context.MODE_PRIVATE)

    var enabled: Boolean
        get() = prefs.getBoolean("enabled", true)
        set(v) { prefs.edit().putBoolean("enabled", v).apply() }

    fun all(): List<String> {
        val a = JSONArray(prefs.getString("items", "[]"))
        return List(a.length()) { a.getString(it) }
    }

    private fun save(l: List<String>) {
        prefs.edit().putString("items", JSONArray(l).toString()).apply()
    }

    fun add(t: String) { save(all() + t) }

    fun update(i: Int, t: String) {
        save(all().toMutableList().also { it[i] = t })
    }

    fun delete(i: Int) {
        save(all().toMutableList().also { it.removeAt(i) })
    }

    fun prompt(): String {
        if (!enabled) return ""
        val l = all()
        if (l.isEmpty()) return ""
        return " The user explicitly saved these memories; use them when relevant: " +
            l.joinToString("; ")
    }
}
