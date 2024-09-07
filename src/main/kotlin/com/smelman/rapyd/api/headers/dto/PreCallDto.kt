package com.smelman.rapyd.api.headers.dto

internal class PreCallResult(
    val salt: String,
    val timestamp: Long,
    val signature: String
)