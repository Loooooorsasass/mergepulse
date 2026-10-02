package com.example.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleHelper {
    fun getLocalizedContext(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            "vi" -> Locale("vi")
            "zh" -> Locale.SIMPLIFIED_CHINESE
            "es" -> Locale("es")
            "pt" -> Locale("pt")
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "ja" -> Locale.JAPANESE
            else -> Locale.ENGLISH
        }
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
