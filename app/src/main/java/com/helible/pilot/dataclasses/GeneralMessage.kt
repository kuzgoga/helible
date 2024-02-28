package com.helible.pilot.dataclasses

// This dataclass provide message content with its type without any markers
data class GeneralMessage(
    val type: MessageType,
    val data: String
)