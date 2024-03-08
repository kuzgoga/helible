package com.helible.pilot.dataclasses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PidSettings (
    @Json(name = "p1") val heightControllerParams: PidParams,
    @Json(name = "p2") val yawControllerParams: PidParams,
    @Json(name = "p3") val pitchControllerParams: PidParams
)
