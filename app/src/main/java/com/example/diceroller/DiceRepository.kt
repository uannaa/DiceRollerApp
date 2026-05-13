package com.example.diceroller

import android.content.Context
import org.json.JSONObject

/**
 * Handles all data persistence via SharedPreferences.
 * Roll histories are stored as JSON objects: { "1": 5, "2": 3, … }
 */
class DiceRepository(private val context: Context) {

    private val prefs
        get() = context.getSharedPreferences("dice_roller_prefs", Context.MODE_PRIVATE)

    // ── Settings ──────────────────────────────────────────────────────────────

    fun isMemoryEnabled(): Boolean = prefs.getBoolean(KEY_MEMORY, false)

    fun setMemoryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MEMORY, enabled).apply()
    }

    fun getIsDarkTheme(): Boolean = prefs.getBoolean(KEY_DARK_THEME, true)

    fun setIsDarkTheme(dark: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_THEME, dark).apply()
    }

    // ── Roll Histories ────────────────────────────────────────────────────────

    fun getRollHistory(diceType: String): Map<Int, Int> {
        val json = prefs.getString(historyKey(diceType), null) ?: return emptyMap()
        return try {
            val obj = JSONObject(json)
            buildMap { obj.keys().forEach { k -> put(k.toInt(), obj.getInt(k)) } }
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun saveRollHistory(diceType: String, history: Map<Int, Int>) {
        val obj = JSONObject().apply { history.forEach { (k, v) -> put(k.toString(), v) } }
        prefs.edit().putString(historyKey(diceType), obj.toString()).apply()
    }

    fun clearAllHistories() {
        prefs.edit().apply { DICE_TYPES.forEach { remove(historyKey(it)) } }.apply()
    }

    fun clearHistory(diceType: String) {
        prefs.edit().remove(historyKey(diceType)).apply()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun historyKey(diceType: String) = "history_$diceType"

    companion object {
        private const val KEY_MEMORY     = "memory_enabled"
        private const val KEY_DARK_THEME = "is_dark_theme"

        val DICE_TYPES = listOf("D4", "D6", "D8", "D10", "D12", "D20")
    }
}