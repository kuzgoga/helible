package com.helible.pilot.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.helible.pilot.controllers.AndroidBluetoothController

@Suppress("UNCHECKED_CAST")
class BluetoothViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BluetoothViewModel(
            bluetoothController = AndroidBluetoothController(context)
        ) as T
    }
}
