package com.smelman.rapyd.api.headers

import com.smelman.rapyd.api.headers.service.HeadersService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.provider.Arguments
import org.mockito.Mockito.*
import java.util.stream.Stream

class RapydApiHeadersTest {

    private lateinit var headersService: HeadersService
    private lateinit var rapydApiHeaders: RapydApiHeaders

    @BeforeEach
    fun setUp() {
        headersService = mock(HeadersService::class.java)
        rapydApiHeaders = RapydApiHeaders(headersService)
    }

    @ParameterizedTest
    @CsvSource(
        "GET, /v1/data/countries, {}, access_key, someSalt, 1234567890, someSignature, 1234567890someSalt",
        "POST, /v1/data/items, sadasdas, access_key, someSalt, 1234567890, someSignature, 1234567890someSalt",
        "PUT, /v1/data/items/123, dsadasd, access_key, someSalt, 1234567890, someSignature, 1234567890someSalt",
        "DELETE, /v1/data/items/123, dasdasdas, access_key, someSalt, 1234567890, someSignature, 1234567890someSalt",
        "HEAD, /v1/data/headers, dasdasd, access_key, someSalt, 1234567890, someSignature, 1234567890someSalt",
        "OPTIONS, /v1/data/options, dasdasdasdasdas, access_key, someSalt, 1234567890, someSignature, 1234567890someSalt"
    )
    fun `generateHeaders should return correct headers`(
        httpMethod: String,
        path: String,
        body: String,
        accessKey: String,
        salt: String,
        timestamp: String,
        signature: String,
        idempotency: String
    ) {
        val expectedHeaders = mapOf(
            "access_key" to accessKey,
            "salt" to salt,
            "timestamp" to timestamp,
            "signature" to signature,
            "idempotency" to idempotency
        )

        `when`(headersService.generateRapydHeaders(anyString(), anyString(), eq(body)))
            .thenReturn(expectedHeaders)

        val actualHeaders = rapydApiHeaders.generateHeaders(httpMethod, path, body)

        assertEquals(expectedHeaders, actualHeaders)
        verify(headersService).generateRapydHeaders(httpMethod, path, body)
    }

    @ParameterizedTest
    @CsvSource(
        "POST, /v1/data/items, X-Custom-Header, CustomValue, access_key, someSalt, 1234567890, someSignature, 1234567890someSalt",
        "GET, /v1/data/countries, X-Another-Custom-Header, AnotherCustomValue, access_key, someSalt, 1234567890, someSignature, 1234567890someSalt"
    )
    fun `generateRapydHeaders with additionalHeaders should merge additional headers`(
        httpMethod: String,
        path: String,
        additionalHeaderKey: String,
        additionalHeaderValue: String,
        accessKey: String,
        salt: String,
        timestamp: String,
        signature: String,
        idempotency: String
    ) {
        val additionalHeaders = mapOf(additionalHeaderKey to additionalHeaderValue)
        val expectedHeaders = mapOf(
            additionalHeaderKey to additionalHeaderValue,
            "access_key" to accessKey,
            "salt" to salt,
            "timestamp" to timestamp,
            "signature" to signature,
            "idempotency" to idempotency
        )

        `when`(headersService.generateRapydHeaders(httpMethod, path, null))
            .thenReturn(expectedHeaders - additionalHeaderKey)

        val actualHeaders =
            rapydApiHeaders.generateRapydHeaders(httpMethod, path, null, additionalHeaders = additionalHeaders)

        assertEquals(expectedHeaders, actualHeaders)
        verify(headersService).generateRapydHeaders(httpMethod, path, null)
    }

    @ParameterizedTest
    @MethodSource("provideTimestampAndSalt")
    fun `generateRapydHeaders with custom salt and timestamp should pass them through`(
        httpMethod: String,
        path: String,
        customSalt: String,
        customTimestamp: Long,
        accessKey: String,
        expectedSalt: String,
        expectedTimestamp: String
    ) {
        val expectedHeaders = mapOf(
            "access_key" to accessKey,
            "salt" to expectedSalt,
            "timestamp" to expectedTimestamp,
            "signature" to "someSignature",
            "idempotency" to "$expectedTimestamp$expectedSalt"
        )

        `when`(headersService.generateRapydHeaders(httpMethod, path, null, customSalt, customTimestamp))
            .thenReturn(expectedHeaders)

        val actualHeaders = rapydApiHeaders.generateRapydHeaders(
            httpMethod, path, null, timestamp = customTimestamp, salt = customSalt
        )

        assertEquals(expectedHeaders, actualHeaders)
        verify(headersService).generateRapydHeaders(httpMethod, path, null, customSalt, customTimestamp)
    }

    companion object {
        @JvmStatic
        fun provideTimestampAndSalt(): Stream<Arguments> = Stream.of(
            Arguments.of(
                "PUT",
                "/v1/data/items/123",
                "customSalt",
                1234567890L,
                "accessKey",
                "customSalt",
                "1234567890"
            ),
            Arguments.of(
                "DELETE",
                "/v1/data/items/123",
                "anotherSalt",
                9876543210L,
                "accessKey",
                "anotherSalt",
                "9876543210"
            ),
            Arguments.of("HEAD", "/v1/data/headers", "headSalt", 1122334455L, "accessKey", "headSalt", "1122334455"),
            Arguments.of(
                "OPTIONS",
                "/v1/data/options",
                "optionsSalt",
                5566778899L,
                "accessKey",
                "optionsSalt",
                "5566778899"
            ),
            Arguments.of("POST", "/v1/data/items", "postSalt", 3344556677L, "accessKey", "postSalt", "3344556677"),
            Arguments.of("GET", "/v1/data/countries", "getSalt", 9988776655L, "accessKey", "getSalt", "9988776655")
        )
    }
}
