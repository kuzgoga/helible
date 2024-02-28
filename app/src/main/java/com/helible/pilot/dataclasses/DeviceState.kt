package com.helible.pilot.dataclasses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DeviceState(
    val status: DeviceStatus = DeviceStatus.ChargeRequired,
    @Json(name = "charge") val batteryCharge: Int = 10,
    val flightHeight: Float = 0f,
    @Json(name = "y") val yaw: Float = 0f,
    @Json(name = "p") val pitch: Float = 0f,
    @Json(name = "r") val roll: Float = 0f,
    @Json(name = "zIn") val zInertial: Float = 0f,
    val pidSettings: PidSettings? = null
)