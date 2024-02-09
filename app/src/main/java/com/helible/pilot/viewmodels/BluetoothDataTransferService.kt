package com.helible.pilot.viewmodels

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.helible.pilot.dataclasses.DeviceState
import com.helible.pilot.dataclasses.DeviceStatusJsonAdapter
import com.squareup.moshi.JsonEncodingException
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

import java.io.IOException

class TransferFailedException : IOException("Reading incoming data failed")

@ExperimentalStdlibApi
class BluetoothDataTransferService(
    private val socket: BluetoothSocket,
) {
    fun listenForIncomingMessages(): Flow<DeviceState> {
        val moshi = Moshi.Builder().add(DeviceStatusJsonAdapter()).add(KotlinJsonAdapterFactory()).build()
        val deviceStateMessageAdapter = moshi.adapter<DeviceState>()
        return flow {
            if (!socket.isConnected)
                return@flow
            val buffer = ByteArray(512)
            while (true) {
                val byteCount: Int = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    Log.e("BluetoothController", "Failed to receive incoming data")
                    throw TransferFailedException()
                }

                val messageData: String = buffer.decodeToString(endIndex = byteCount)
                if (!messageData.endsWith("\n\r")) {
                    Log.i("BluetoothController", "Package end isn't valid.")
                    Log.i("BluetoothController", messageData)

                } else {
                    val messageJson = messageData.dropLast(2)
                    try {
                        val deviceState = deviceStateMessageAdapter.fromJson(messageJson)!!
                        emit(deviceState)
                        Log.i("BluetoothController", "Received: $deviceState")
                    } catch (e: NullPointerException) {
                        Log.e("BluetoothController", "Nullable message received: $messageJson")
                    } catch (e: JsonEncodingException) {
                        Log.e("BluetoothController", "Invalid message received: $messageJson")
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessage(bytes: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(bytes)
            } catch (e: IOException) {
                Log.e("BluetoothController", "Failed to write message: $e")
                return@withContext false
            }
            true
        }
    }
}

