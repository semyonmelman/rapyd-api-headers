package com.smelman.rapyd.api.headers.util

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.time.Instant
import kotlin.math.abs

internal class UtilsKtTest {

    @Test
    fun `generateSalt should produce a string of the specified length`() {
        val length = 12
        val salt = generateSalt(length)
        assertEquals(length, salt.length, "Generated salt length should be $length")
    }

    @Test
    fun `generateSalt should produce a string of default length 12`() {
        val salt = generateSalt()
        assertEquals(12, salt.length, "Generated salt length should be 12 by default")
    }

    @Test
    fun `generateSalt should produce a string with only valid characters`() {
        val salt = generateSalt()
        assertTrue(salt.all { it in "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789" },
            "Generated salt should only contain valid characters")
    }

    @Test
    fun `getUnixTime should return a value close to the current time`() {
        val currentTime = Instant.now().epochSecond
        val unixTime = getUnixTime()

        // Allow a small margin of error, e.g., 1 second
        val margin = 1
        assertTrue(
            abs(currentTime - unixTime) <= margin,
            "Unix time should be within $margin seconds of the current time")
    }
}