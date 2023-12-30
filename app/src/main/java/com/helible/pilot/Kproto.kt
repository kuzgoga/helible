package com.helible.pilot

// Todo: add checksum
// Todo: add arguments names

data class KMessage(
    val r1: UShort,
    val r2: UShort,
    val r3: UShort,
    val emergStop: Boolean,
    val alarm: Boolean,
)

fun KMessage.toByteArray(): ByteArray {
    return "$$r1;$r2;$r3;$emergStop;$alarm\r\n".encodeToByteArray()
}

fun String.toKMessage(): KMessage {
    // TODO: implement
    return KMessage(
        0u, 0u, 0u, emergStop = false, alarm = false
    )
}