package com.example.diceroller

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel

/**
 * Shared ViewModel for Main and Settings screens.
 * Holds per-dice roll histories in a SnapshotStateMap so Compose reacts to changes.
 */
class DiceViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = DiceRepository(application)

    // ── Settings state ────────────────────────────────────────────────────────

    var isMemoryEnabled by mutableStateOf(repo.isMemoryEnabled())
        private set

    var isDarkTheme by mutableStateOf(repo.getIsDarkTheme())
        private set

    // ── Roll histories (dice type → face → count) ─────────────────────────────

    private val _histories = mutableStateMapOf<String, Map<Int, Int>>()

    /** Returns the current roll-count map for a given dice type. */
    fun historyFor(diceType: String): Map<Int, Int> = _histories[diceType] ?: emptyMap()

    init {
        // Load persisted histories on startup only when memory is enabled
        if (isMemoryEnabled) loadAllFromDisk()
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /** Call after every manual roll. */
    fun recordRoll(diceType: String, value: Int) {
        val updated = (_histories[diceType] ?: emptyMap()).toMutableMap()
        updated[value] = (updated[value] ?: 0) + 1
        _histories[diceType] = updated
        if (isMemoryEnabled) repo.saveRollHistory(diceType, updated)
    }

    /** Generate [count] random rolls for [diceType] and merge into the existing history. */
    fun simulate(diceType: String, count: Int) {
        val range   = diceRange(diceType)
        val updated = (_histories[diceType] ?: emptyMap()).toMutableMap()
        repeat(count) {
            val v = range.random()
            updated[v] = (updated[v] ?: 0) + 1
        }
        _histories[diceType] = updated
        if (isMemoryEnabled) repo.saveRollHistory(diceType, updated)
    }

    fun toggleMemory(enabled: Boolean) {
        isMemoryEnabled = enabled
        repo.setMemoryEnabled(enabled)
        // If the user just enabled memory, persist what is already in memory
        if (enabled) {
            _histories.forEach { (dice, hist) -> repo.saveRollHistory(dice, hist) }
        }
    }

    fun toggleTheme(dark: Boolean) {
        isDarkTheme = dark
        repo.setIsDarkTheme(dark)
    }

    /** Clear all dice histories (both in-memory and on disk). */
    fun clearAll() {
        _histories.clear()
        repo.clearAllHistories()
    }

    /** Clear history for a single dice type. */
    fun clearOne(diceType: String) {
        _histories.remove(diceType)
        repo.clearHistory(diceType)
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun loadAllFromDisk() {
        DiceRepository.DICE_TYPES.forEach { dt ->
            val h = repo.getRollHistory(dt)
            if (h.isNotEmpty()) _histories[dt] = h
        }
    }

    companion object {
        fun diceRange(diceType: String): IntRange = when (diceType) {
            "D4"  -> 1..4
            "D6"  -> 1..6
            "D8"  -> 1..8
            "D10" -> 1..10
            "D12" -> 1..12
            "D20" -> 1..20
            else  -> 1..6
        }

        fun diceMaxFace(diceType: String): Int = when (diceType) {
            "D4"  -> 4
            "D6"  -> 6
            "D8"  -> 8
            "D10" -> 10
            "D12" -> 12
            "D20" -> 20
            else  -> 6
        }
    }
}