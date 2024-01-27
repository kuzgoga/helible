package com.helible.pilot.controllers

data class DeviceState(
    val isHandshakeWaiting: Boolean = true,
    val isIMUCalibrating: Boolean = false,
    val flightMode: Boolean = false,
    val batteryCharge: Int?,
    val flightHeight: Float?
)