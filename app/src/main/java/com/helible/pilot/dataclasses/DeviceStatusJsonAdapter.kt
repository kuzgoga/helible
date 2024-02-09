package com.helible.pilot.dataclasses

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson

class DeviceStatusJsonAdapter {
    @FromJson
    fun fromJson(deviceStatus: String): DeviceStatus {
        try {
            val index: UInt = deviceStatus.toUInt()
            return DeviceStatus.values()[index.toInt()]
        } catch (e: IndexOutOfBoundsException) {
            throw JsonDataException("Impossible conversation from String to DeviceStatus")
        }
    }

    @ToJson
    fun toJson(deviceStatus: DeviceStatus): String {
        return DeviceStatus.values().indexOf(deviceStatus).toString()
    }
}