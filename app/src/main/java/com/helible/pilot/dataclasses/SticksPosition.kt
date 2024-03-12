package com.helible.pilot.dataclasses

import com.squareup.moshi.Json

data class SticksPosition(
    @Json(name = "hS") val heightStick: Int,
    @Json(name = "yS") val yawStick: Int,
    @Json(name = "pS") val pitchStick: Int
)

