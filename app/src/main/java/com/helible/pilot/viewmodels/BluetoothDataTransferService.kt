package com.helible.pilot.viewmodels

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.helible.pilot.dataclasses.GeneralMessage
import com.helible.pilot.dataclasses.MessageType
import com.helible.pilot.exceptions.TransferFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.IOException

const val maxPackageSize = 512 // [bytes]

class BluetoothDataTransferService(
    private val socket: BluetoothSocket,
) {
    fun listenForIncomingMessages(): Flow<GeneralMessage> {
        return flow {
            if (!socket.isConnected)
                return@flow
            val buffer = BufferedInputStream(socket.inputStream, maxPackageSize)
            while (true) {
                val message: String = try {
                    buffer.bufferedReader().readLine()
                } catch (e: IOException) {
                    Log.e("BluetoothController", "Failed to receive incoming data")
                    throw TransferFailedException()
                }
                try {
                    val messageType =
                        MessageType.values().elementAtOrNull(message.split(";").first().toInt())
                    val messageData = message.split(";").last()
                    if (messageType != null) {
                        emit(GeneralMessage(messageType, messageData))
                    }
                } catch (e: NoSuchElementException) {
                    Log.e("BluetoothController", "Message type is invalid: ${e.message}")
                } catch (e: NumberFormatException) {
                    Log.e("BluetoothController", "Message invalid, may be device buffer congested: ${e.message}")
                } catch (e: Exception) {
                    Log.e("BluetoothController", "Unknown error: ${e.message}")
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

