package com.nikodindon.retrocade.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nikodindon.retrocade.core.common.ControlScheme
import com.nikodindon.retrocade.core.common.GestureSensitivity
import dagger.hilt.android.qualifiers.ApplicationContext
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

/**
 * Kept in a separate file so backup rules can exclude the Premium cache without
 * excluding the user's settings and scores.
 */
val Context.premiumDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "premium",
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

    suspend fun setSfxVolume(volume: Int) = update {
        it.copy(sfxVolume = volume.coerceIn(VOLUME_MIN, VOLUME_MAX))
    }

    suspend fun setMusicVolume(volume: Int) = update {
        it.copy(musicVolume = volume.coerceIn(VOLUME_MIN, VOLUME_MAX))
    }

    suspend fun setHapticsEnabled(enabled: Boolean) = update { it.copy(hapticsEnabled = enabled) }

    suspend fun setControlScheme(scheme: ControlScheme) = update {
        it.copy(controlScheme = scheme.name)
    }

    suspend fun setGestureSensitivity(sensitivity: GestureSensitivity) = update {
        it.copy(gestureSensitivity = sensitivity.name)
    }

    suspend fun setOnboardingDone(done: Boolean) = update { it.copy(onboardingDone = done) }

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

    private companion object {
        const val VOLUME_MIN = 0
        const val VOLUME_MAX = 100
    }
}

@Singleton
class ScoreRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val mutex = Mutex()

    val scores: Flow<ScoreBook> = dataStore.data.map { preferences ->
        ScoreCodec.decode(preferences[Keys.SCORES])
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
            preferences[Keys.SCORES] = ScoreCodec.encode(updated)
        }
        result
    }

    /** Removes every local score. Settings asks for confirmation before calling this. */
    suspend fun reset() {
        mutex.withLock {
            dataStore.edit { preferences ->
                preferences.remove(Keys.SCORES)
            }
        }
    }
}

@Singleton
class PremiumCacheStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : PremiumCache {
    private val dataStore: DataStore<Preferences>
        get() = context.premiumDataStore

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
