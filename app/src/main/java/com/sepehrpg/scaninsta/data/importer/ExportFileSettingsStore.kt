package com.sepehrpg.scaninsta.data.importer

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Persists import file-name overrides without requiring storage permissions. */
@Singleton
class ExportFileSettingsStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(load())

    val settings: StateFlow<ExportFileSettings> = _settings.asStateFlow()

    fun update(followersFileName: String, followingFileName: String) {
        val updated = ExportFileSettings(
            followersFileName = followersFileName.trim(),
            followingFileName = followingFileName.trim(),
        )
        preferences.edit()
            .putString(FOLLOWERS_FILE_NAME_KEY, updated.followersFileName)
            .putString(FOLLOWING_FILE_NAME_KEY, updated.followingFileName)
            .apply()
        _settings.value = updated
    }

    fun reset() {
        val defaults = ExportFileSettings()
        preferences.edit().clear().apply()
        _settings.value = defaults
    }

    private fun load(): ExportFileSettings = ExportFileSettings(
        followersFileName = preferences.getString(
            FOLLOWERS_FILE_NAME_KEY,
            ExportFileSettings.DEFAULT_FOLLOWERS_FILE_NAME,
        ).orEmpty().ifBlank { ExportFileSettings.DEFAULT_FOLLOWERS_FILE_NAME },
        followingFileName = preferences.getString(
            FOLLOWING_FILE_NAME_KEY,
            ExportFileSettings.DEFAULT_FOLLOWING_FILE_NAME,
        ).orEmpty().ifBlank { ExportFileSettings.DEFAULT_FOLLOWING_FILE_NAME },
    )

    private companion object {
        const val PREFERENCES_NAME = "instagram_export_file_settings"
        const val FOLLOWERS_FILE_NAME_KEY = "followers_file_name"
        const val FOLLOWING_FILE_NAME_KEY = "following_file_name"
    }
}
