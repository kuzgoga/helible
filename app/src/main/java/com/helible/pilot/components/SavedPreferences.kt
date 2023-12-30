package com.helible.pilot.components

import android.content.SharedPreferences
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@JsonClass(generateAdapter = true)
data class AppPreferences(
    val deviceName: String,
    val deviceAddress: String,
)

interface SavedPreferences {
    fun getPreferences(): AppPreferences?
    fun savePreferences(preferences: AppPreferences)
    fun clearPreferences()
}

class SavedPreferencesImpl(private val sharedPreferences: SharedPreferences) : SavedPreferences {
    private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val preferencesAdapter: JsonAdapter<AppPreferences> =
        moshi.adapter(AppPreferences::class.java)

    override fun getPreferences(): AppPreferences? {
        val json = sharedPreferences.getString("preferences", null) ?: return null
        return preferencesAdapter.fromJson(json)
    }

    override fun savePreferences(preferences: AppPreferences) {
        sharedPreferences.edit()
            .putString("preferences", preferencesAdapter.toJson(preferences))
            .apply()

    }

    override fun clearPreferences() {
        sharedPreferences.edit().remove("preferences").apply()
    }
}