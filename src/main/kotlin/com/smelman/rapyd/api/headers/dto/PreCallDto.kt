package com.smelman.rapyd.api.headers.dto

class PreCallResult(
    val salt: String,
    val timestamp: Long,
    val signature: String
)