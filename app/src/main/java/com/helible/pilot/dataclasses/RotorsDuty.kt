package com.helible.pilot.dataclasses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RotorsDuty(
    val r1: Short,
    val r2: Short,
    val r3: Short
)