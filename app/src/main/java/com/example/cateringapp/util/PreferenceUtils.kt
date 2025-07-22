package com.example.cateringapp.util

import android.content.Context
import android.net.Uri

object PreferenceUtils {
    private const val PREF_NAME = "header_footer_prefs"
    private const val KEY_HEADER_URI = "header_uri"
    private const val KEY_FOOTER_URI = "footer_uri"

    fun saveHeaderUri(context: Context, uri: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_HEADER_URI, uri).apply()
    }

    fun saveFooterUri(context: Context, uri: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_FOOTER_URI, uri).apply()
    }

    fun getHeaderUri(context: Context): Uri? {
        val uriStr = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_HEADER_URI, null)
        return uriStr?.let { Uri.parse(it) }
    }

    fun getFooterUri(context: Context): Uri? {
        val uriStr = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_FOOTER_URI, null)
        return uriStr?.let { Uri.parse(it) }
    }
}
