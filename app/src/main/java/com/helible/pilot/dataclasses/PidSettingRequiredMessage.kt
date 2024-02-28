package com.helible.pilot.dataclasses

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PidSettingRequiredMessage (
    val pidSettingOpened: Boolean = true
)