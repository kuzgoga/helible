package com.helible.pilot.viewmodels

import android.bluetooth.BluetoothSocket
import android.util.Log
import com.helible.pilot.KMessage
import com.helible.pilot.toKMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

import java.io.IOException

class TransferFailedException : IOException("Reading incoming data failed")

class BluetoothDataTransferService(
    private val socket: BluetoothSocket,
) {
    fun listenForIncomingMessages(): Flow<String> {
        return flow {
            if (!socket.isConnected)
                return@flow
            val buffer = ByteArray(128)
            while (true) {
                val byteCount: Int = try {
                    socket.inputStream.read(buffer)
                } catch (e: IOException) {
                    throw TransferFailedException()
                }
                val strData: String = buffer.decodeToString(endIndex = byteCount)
                emit(
                    strData
                )
                Log.i("BluetoothController", "Received: ${strData.dropLast(2)}")
            }
        }.flowOn(Dispatchers.IO)
    }

    suspend fun sendMessage(bytes: ByteArray): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                socket.outputStream.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
                return@withContext false
            }
            true
        }
    }
}