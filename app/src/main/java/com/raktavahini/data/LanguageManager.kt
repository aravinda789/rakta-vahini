package com.raktavahini.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    KANNADA("kn", "ಕನ್ನಡ")
}

@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)

    fun getSelectedLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
    }

    fun setLanguage(languageCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    fun isLanguageSelected(): Boolean {
        return prefs.contains(KEY_LANGUAGE)
    }

    companion object {
        private const val KEY_LANGUAGE = "selected_language"
    }
}