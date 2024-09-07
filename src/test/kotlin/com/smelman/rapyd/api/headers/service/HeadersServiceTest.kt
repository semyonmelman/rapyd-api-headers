package com.smelman.rapyd.api.headers.service

import com.smelman.rapyd.api.headers.util.getUnixTime
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class HeadersServiceTest {

    private lateinit var headersService: HeadersService
    private val accessKey = "testAccessKey"
    private val secretKey = "testSecretKey"

    @BeforeEach
    fun setup() {
        headersService = HeadersService(accessKey, secretKey)
    }

    @ParameterizedTest
    @CsvSource("get", "post", "put", "delete", "head", "options")
    fun `generateRapydHeaders should generate correct headers`(httpMethod: String) {
        // Mock the dependencies
        val path = "/v1/test"
        val body = mapOf("key" to "value")

        // Set up expected values
        val timestamp = getUnixTime()
        val mac = Mac.getInstance(ALGORITHM)
        val secretKeySpec = SecretKeySpec(secretKey.encodeToByteArray(), ALGORITHM)
        mac.init(secretKeySpec)

        // Generate headers
        val headers = headersService.generateRapydHeaders(httpMethod, path, body)

        // Validate headers
        assertEquals(accessKey, headers["access_key"], "Access key should match")
        assertTrue(headers["salt"]!!.length == 12, "Salt should be 12 characters long")
        assertEquals(timestamp.toString(), headers["timestamp"], "Timestamp should match")
        assertNotNull(headers["signature"])
        assertTrue(headers["idempotency"]!!.startsWith("$timestamp"), "Idempotency should start with timestamp")
    }

    @ParameterizedTest
    @CsvSource("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS")
    fun `generateRapydHeaders should handle empty body correctly`(httpMethod: String) {
        // Mock the dependencies
        val path = "/v1/test"
        val body: Map<String, Any>? = null

        // Set up expected values
        val timestamp = getUnixTime()
        val mac = Mac.getInstance(ALGORITHM)
        val secretKeySpec = SecretKeySpec(secretKey.encodeToByteArray(), ALGORITHM)
        mac.init(secretKeySpec)
        // Generate headers
        val headers = headersService.generateRapydHeaders(httpMethod, path, body)

        // Validate headers
        assertEquals(accessKey, headers["access_key"], "Access key should match")
        assertTrue(headers["salt"]!!.length == 12, "Salt should be 12 characters long")
        assertEquals(timestamp.toString(), headers["timestamp"], "Timestamp should match")
        assertNotNull(headers["signature"])
        assertTrue(headers["idempotency"]!!.startsWith("$timestamp"), "Idempotency should start with timestamp")
    }

    @Test
    fun `generateRapydHeaders should should throw IllegalArgumentException when httpMethod is wrong`() {
        // Mock the dependencies
        val httpMethod = "someWrongHttpMethod"
        val path = "/v1/test"
        val body: Map<String, Any>? = null

        // Generate headers
        val exception = assertThrows<IllegalArgumentException> {
            headersService.generateRapydHeaders(httpMethod, path, body)
        }
        assertEquals(
            "Method someWrongHttpMethod is not supported, supported methods: $SUPPORTED_HTTP_METHODS",
            exception.message
        )
    }

    @Test
    fun `generateRapydHeaders should should throw IllegalArgumentException when path is wrong`() {
        // Mock the dependencies
        val httpMethod = "GET"
        val path = "v1/test"
        val body: Map<String, Any>? = null

        // Generate headers
        val exception = assertThrows<IllegalArgumentException> {
            headersService.generateRapydHeaders(httpMethod, path, body)
        }
        assertEquals(
            "Path variable should start from '/'",
            exception.message
        )
    }
}
