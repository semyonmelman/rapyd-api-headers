package com.smelman.rapyd.api.headers.util

import java.time.Instant

private const val CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"

fun generateSalt(length: Int = 12): String {
    return (1..length).map { CHARS.random() }.joinToString("")
}

fun getUnixTime(): Long {
    return Instant.now().epochSecond
}