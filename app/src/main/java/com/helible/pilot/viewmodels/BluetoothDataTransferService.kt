package com.helible.pilot.viewmodels

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.helible.pilot.dataclasses.DeviceState
import com.helible.pilot.dataclasses.DeviceStatusJsonAdapter
import com.helible.pilot.dataclasses.GeneralMessage
import com.helible.pilot.dataclasses.MessageType
import com.helible.pilot.dataclasses.PidSettings
import com.helible.pilot.exceptions.TransferFailedException
import com.squareup.moshi.JsonDataException
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

const val maxPackageSize = 512; // bytes

@ExperimentalStdlibApi
class BluetoothDataTransferService(
    private val socket: BluetoothSocket,
) {
    fun listenForIncomingMessages(): Flow<GeneralMessage> {
        return flow {
            if (!socket.isConnected)
                return@flow

            val buffer = ByteArray(maxPackageSize)
            while (true) {
                val byteCount: Int = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    Log.e("BluetoothController", "Failed to receive incoming data")
                    throw TransferFailedException()
                }
                var messageData: String = buffer.decodeToString(endIndex = byteCount)
                val messageType: MessageType? = MessageType.values()
                    .elementAtOrNull(messageData.split(";")[0].toInt())

                if (messageData.endsWith("\n\r") && messageType != null) {
                    messageData = messageData.dropLast(2).split(";")[1]
                    emit(GeneralMessage(messageType, messageData))
                    Log.d("BluetoothController", "Received: $messageData")
                } else {
                    Log.i("BluetoothController", "Package end isn't valid.")
                    Log.i("BluetoothController", messageData)
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

