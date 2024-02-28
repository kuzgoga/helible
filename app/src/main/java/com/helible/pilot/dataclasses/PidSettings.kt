package com.helible.pilot.dataclasses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PidSettings (
    val p1: PidParams,
    val p2: PidParams,
    val p3: PidParams
)
