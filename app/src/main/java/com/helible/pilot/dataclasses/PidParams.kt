package com.helible.pilot.dataclasses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PidParams (
    val p: Float,
    val i: Float,
    val d: Float
)