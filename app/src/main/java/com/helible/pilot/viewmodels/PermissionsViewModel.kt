package com.helible.pilot.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.helible.pilot.components.AppPreferences
import com.helible.pilot.components.SavedPreferences

class PermissionDialogViewModel : ViewModel() {
    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeFirst()
    }

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        }
    }
}

class PreferencesViewModel(
    private val preferencesStorage: SavedPreferences,
) : ViewModel() {
    val preferences: AppPreferences? get() = preferencesStorage.getPreferences()
    fun savePreferences(savedPreferences: AppPreferences) {
        preferencesStorage.savePreferences(
            preferences = savedPreferences
        )
    }

    fun clearPreferences() {
        preferencesStorage.clearPreferences()
    }
}