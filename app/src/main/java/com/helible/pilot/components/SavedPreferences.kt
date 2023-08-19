package com.helible.pilot.components

import android.content.SharedPreferences
import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize


@Parcelize
data class SavedPreferences(
    val deviceAddress: String?
): Parcelable

interface SavedPreferencesCache {
    fun getPreferences(): SavedPreferences?
    fun savePreferences(preferences: SavedPreferences)
    fun clearPreferences()
}

class PreferencesCacheImpl(private val sharedPreferences: SharedPreferences) : SavedPreferencesCache {
    override fun getPreferences(): SavedPreferences? {
        val json = sharedPreferences.getString("preferences", null) ?: return null
        return Gson().fromJson(json, SavedPreferences::class.java)
    }
    override fun savePreferences(preferences: SavedPreferences)
    {
        sharedPreferences.edit()
            .putString("preferences", Gson().toJson(preferences))
            .apply()

    }

    override fun clearPreferences()
    {
        sharedPreferences.edit().remove("preferences").apply()
    }
}