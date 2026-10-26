package com.nikodindon.retrocade.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nikodindon.retrocade.core.common.ControlScheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

val Context.retrocadeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "retrocade",
)

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        decodeSettings(preferences[Keys.SETTINGS])
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun setSfxEnabled(enabled: Boolean) = update { it.copy(sfxEnabled = enabled) }

    suspend fun setMusicEnabled(enabled: Boolean) = update { it.copy(musicEnabled = enabled) }

    suspend fun setHapticsEnabled(enabled: Boolean) = update { it.copy(hapticsEnabled = enabled) }

    suspend fun setControlScheme(scheme: ControlScheme) = update {
        it.copy(controlScheme = scheme.name)
    }

    suspend fun setSwipeSensitivity(sensitivity: Float) = update {
        it.copy(swipeSensitivity = sensitivity.coerceIn(0f, 1f))
    }

    suspend fun setShowHoldButton(show: Boolean) = update { it.copy(showHoldButton = show) }

    private suspend fun update(transform: (AppSettings) -> AppSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.SETTINGS] = json.encodeToString(
                AppSettings.serializer(),
                transform(decodeSettings(preferences[Keys.SETTINGS])),
            )
        }
    }

    private fun decodeSettings(raw: String?): AppSettings =
        raw?.let {
            runCatching { json.decodeFromString(AppSettings.serializer(), it) }.getOrNull()
        } ?: AppSettings()
}

@Singleton
class ScoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val mutex = Mutex()

    val scores: Flow<ScoreBook> = dataStore.data.map { preferences ->
        decodeScores(preferences[Keys.SCORES])
    }

    suspend fun current(): ScoreBook = scores.first()

    suspend fun record(
        gameId: String,
        score: Int,
        lines: Int,
        level: Int,
        timestamp: Long = System.currentTimeMillis(),
    ): RecordResult = mutex.withLock {
        val current = current()
        val entry = ScoreEntry(score = score, lines = lines, level = level, timestamp = timestamp)
        val (updated, result) = ScoreRanking.insert(current, gameId, entry)
        dataStore.edit { preferences ->
            preferences[Keys.SCORES] = json.encodeToString(ScoreBook.serializer(), updated)
        }
        result
    }

    private fun decodeScores(raw: String?): ScoreBook =
        raw?.let {
            runCatching { json.decodeFromString(ScoreBook.serializer(), it) }.getOrNull()
        } ?: ScoreBook()
}

@Singleton
class PremiumCacheStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PremiumCache {
    override suspend fun read(): CachedPremium? {
        val preferences = dataStore.data.first()
        val type = preferences[Keys.PREMIUM_TYPE] ?: return null
        return CachedPremium(
            type = type,
            cachedAtEpochMillis = preferences[Keys.PREMIUM_CACHED_AT] ?: 0L,
        )
    }

    override suspend fun write(premium: CachedPremium?) {
        dataStore.edit { preferences ->
            if (premium == null) {
                preferences.remove(Keys.PREMIUM_TYPE)
                preferences.remove(Keys.PREMIUM_CACHED_AT)
            } else {
                preferences[Keys.PREMIUM_TYPE] = premium.type
                preferences[Keys.PREMIUM_CACHED_AT] = premium.cachedAtEpochMillis
            }
        }
    }
}

interface PremiumCache {
    suspend fun read(): CachedPremium?

    suspend fun write(premium: CachedPremium?)
}

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

private object Keys {
    val SETTINGS = stringPreferencesKey("settings_json")
    val SCORES = stringPreferencesKey("scores_json")
    val PREMIUM_TYPE = stringPreferencesKey("premium_type")
    val PREMIUM_CACHED_AT = longPreferencesKey("premium_cached_at")
}
