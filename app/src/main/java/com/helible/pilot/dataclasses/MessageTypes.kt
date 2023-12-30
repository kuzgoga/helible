package com.helible.pilot.dataclasses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RotorsSpeedMessage(val r1: Short, val r2: Short, val r3: Short)

@JsonClass(generateAdapter = true)
data class EmergStopMessage(val emergStop: Boolean)

@JsonClass(generateAdapter = true)
data class AlarmStateMessage(val isAlarmOn: Boolean)