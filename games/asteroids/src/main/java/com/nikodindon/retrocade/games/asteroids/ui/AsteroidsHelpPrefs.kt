package com.nikodindon.retrocade.games.asteroids.ui

import android.content.Context

/** Remembers whether the one-time Asteroids help sheet has been dismissed. */
internal object AsteroidsHelpPrefs {
    private const val PREFS = "retrocade_asteroids_help"
    private const val KEY_SEEN = "seen"

    fun hasSeen(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_SEEN, false)

    fun markSeen(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SEEN, true)
            .apply()
    }
}
