package me.marthia.avanegar.presentation.utils

import android.content.Context

object LocaleProvider {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    private const val DEFAULT_LANGUAGE = "fa"

    fun onAttach(
        context: Context
    ): Context {
        val lang = getPersistedData(context)
        return setLocale(context, lang)
    }

    fun getLanguage(context: Context): String {
        return getPersistedData(context)
    }

    fun setLocale(context: Context, language: String): Context {
        persist(context, language)
        return updateResources(context, language)
    }

    private fun getPersistedData(context: Context): String {
        val preferences = context.getSharedPreferences("language", Context.MODE_PRIVATE)
        return preferences.getString(SELECTED_LANGUAGE, DEFAULT_LANGUAGE)!!
    }

    fun persist(context: Context, language: String) {
        val preferences = context.getSharedPreferences("language", Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(SELECTED_LANGUAGE, language)
        editor.apply()
    }

    private fun updateResources(
        context: Context,
        language: String,
    ): Context {
        return context.setLocale(language)
    }


}