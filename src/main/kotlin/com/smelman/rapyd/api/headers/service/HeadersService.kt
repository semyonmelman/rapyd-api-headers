package com.smelman.rapyd.api.headers.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.smelman.rapyd.api.headers.dto.PreCallResult
import com.smelman.rapyd.api.headers.util.generateSalt
import com.smelman.rapyd.api.headers.util.getUnixTime
import org.slf4j.LoggerFactory
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * The HmacSHA256 algorithm combines HMAC (Hash-based Message Authentication Code) with
 * the SHA-256 (Secure Hash Algorithm 256-bit) hashing algorithm. It is used to ensure the integrity and
 * authenticity of a message by generating a message authentication code (MAC).
 */
internal const val ALGORITHM = "HmacSHA256"

/**
 * The "%02x" format specifier is used in Kotlin (and Java) to format a byte as a two-character hexadecimal string.
 *
 * %: Marks the beginning of a format specifier.
 * 02: Ensures the output is at least 2 characters wide, and pads with leading zeroes if necessary.
 * x: Converts the byte to a lowercase hexadecimal string.
 */
internal const val HEXADECIMAL_FORMATTER = "%02x"

internal val SUPPORTED_HTTP_METHODS = setOf("get", "post", "put", "delete", "head", "options")

/**
 * Service class for generating Rapyd API headers, including HMAC signature.
 *
 * **IMPORTANT:** Never share your access key and secret key with unauthorized individuals or services.
 *
 * @param accessKey The access key for the Rapyd API.
 * @param secretKey The secret key for the Rapyd API.
 * @param objectMapper An optional [ObjectMapper] instance for JSON processing. If not provided,
 *                     a default [ObjectMapper] instance will be used.
 */
internal class HeadersService(
    private val accessKey: String,
    private val secretKey: String,
    private val objectMapper: ObjectMapper = ObjectMapper()
) {

    init {
        if (log.isDebugEnabled) {
            log.warn(
                "DEBUG mode is enabled! Sensitive information such as signature " +
                        "may be printed in the logs. NEVER enable DEBUG logging in production for this package."
            )
        }
    }

    /**
     * Generates the necessary headers for a Rapyd API call, including the HMAC signature.
     *
     * @param httpMethod The HTTP method (e.g., "GET", "POST").
     * @param path The request path (e.g., "/v1/data/countries"). For more information, see the Rapyd API documentation.
     * @param body Optional request body.
     * @return A map of headers including the access key, salt, timestamp, and signature.
     */
    fun <T> generateRapydHeaders(httpMethod: String, path: String, body: T? = null): Map<String, String> {
        return generateHeaders(httpMethod, path, body)
    }

    /**
     * Generates the necessary headers for a Rapyd API call with custom salt and timestamp, including the HMAC signature.
     *
     * @param httpMethod The HTTP method (e.g., "GET", "POST").
     * @param path The request path (e.g., "/v1/data/countries"). For more information, see the Rapyd API documentation.
     * @param body Optional request body.
     * @param salt Optional custom salt.
     * @param timestamp Optional custom timestamp.
     * @return A map of headers including the access key, custom salt, custom timestamp, and signature.
     */
    fun <T> generateRapydHeaders(
        httpMethod: String,
        path: String,
        body: T? = null,
        salt: String? = null,
        timestamp: Long? = null,
    ): Map<String, String> {
        val (customSalt, customTimestamp, signature) = updateTimestampSaltSig(
            httpMethod,
            path,
            body?.let { objectMapper.writeValueAsString(it) } ?: "",
            salt,
            timestamp
        )
        return createSigHeaders(
            salt = customSalt,
            timestamp = customTimestamp,
            signature = signature
        )
    }

    /**
     * Generates the necessary headers for a Rapyd API call, including the HMAC signature.
     *
     * @param httpMethod The HTTP method (e.g., "GET", "POST").
     * @param path The request path (e.g., "/v1/data/countries"). For more information, see the Rapyd API documentation.
     * @param body Optional request body.
     * @return A map of headers including the access key, salt, timestamp, and signature.
     */
    private fun <T> generateHeaders(httpMethod: String, path: String, body: T? = null): Map<String, String> {
        log.debug("Generating Rapyd API headers for HTTP method: $httpMethod, path: $path, body: $body")
        validateParameters(httpMethod, path)
        val preCallResult = preCall(httpMethod, path, body)
        val headers = createSigHeaders(
            salt = preCallResult.salt,
            timestamp = preCallResult.timestamp,
            signature = preCallResult.signature
        )
        log.debug("Generated headers: $headers")
        return headers
    }

    /**
     * Validates the provided HTTP method and path.
     *
     * @param httpMethod The HTTP method.
     * @param path The request path.
     * @throws IllegalArgumentException If the HTTP method is not supported or the path does not start with '/'.
     */
    private fun validateParameters(httpMethod: String, path: String) {
        require(httpMethod.lowercase() in SUPPORTED_HTTP_METHODS) {
            "Method $httpMethod is not supported. Supported methods are: $SUPPORTED_HTTP_METHODS"
        }
        require(path.startsWith("/")) {
            "Path must start with '/'"
        }
    }

    /**
     * Prepares the data needed for the signature generation: salt, timestamp, and HMAC signature.
     *
     * @param httpMethod The HTTP method.
     * @param path The request path.
     * @param body Optional request body.
     * @return A [PreCallResult] containing the salt, timestamp, and signature.
     */
    private fun <T> preCall(httpMethod: String, path: String, body: T? = null): PreCallResult {
        log.debug("Preparing pre-call data for HTTP method: $httpMethod, path: $path")
        val strBody = body?.let { objectMapper.writeValueAsString(it) }
        val (salt, timestamp, signature) = updateTimestampSaltSig(httpMethod, path, strBody)
        log.debug("Pre-call data: salt=$salt, timestamp=$timestamp, signature=$signature")
        return PreCallResult(salt, timestamp, signature)
    }

    /**
     * Creates the header map with the necessary values including salt, timestamp, and signature.
     *
     * @param salt The salt value.
     * @param timestamp The timestamp value.
     * @param signature The generated HMAC signature.
     * @return A map of headers.
     */
    private fun createSigHeaders(salt: String, timestamp: Long, signature: String): Map<String, String> {
        log.debug("Creating signature headers with salt=$salt, timestamp=$timestamp, signature=$signature")
        return mapOf(
            "access_key" to accessKey,
            "salt" to salt,
            "timestamp" to timestamp.toString(),
            "signature" to signature,
            "idempotency" to "${getUnixTime()}$salt"
        )
    }

    /**
     * Generates a salt, timestamp, and HMAC signature based on the provided parameters.
     *
     * @param httpMethod The HTTP method.
     * @param path The request path.
     * @param body Optional request body.
     * @param customSalt Optional custom salt.
     * @param customTimestamp Optional custom timestamp.
     * @return A [Triple] containing the salt, timestamp, and signature.
     */
    private fun updateTimestampSaltSig(
        httpMethod: String,
        path: String,
        body: String? = null,
        customSalt: String? = null,
        customTimestamp: Long? = null
    ): Triple<String, Long, String> {
        log.debug("Updating timestamp, salt, and signature for HTTP method: $httpMethod, path: $path")
        val httpMethodLowerCase = httpMethod.lowercase()
        val salt = customSalt ?: generateSalt()
        val timestamp = customTimestamp ?: getUnixTime()
        val toSign = "$httpMethodLowerCase$path$salt$timestamp$accessKey$secretKey${body ?: ""}"

        val strHashCode = hmacSha256(toSign, secretKey)
        log.debug("Generated HMAC signature: $strHashCode")
        return Triple(salt, timestamp, strHashCode)
    }

    /**
     * Computes the HMAC SHA-256 hash for the provided data using the secret key.
     *
     * @param data The data to sign.
     * @param secretKey The secret key used for signing.
     * @return The Base64-encoded signature.
     */
    private fun hmacSha256(data: String, secretKey: String): String {
        log.debug("Generating HMAC SHA-256 hash for data: $data")
        val mac = Mac.getInstance(ALGORITHM)
        val secretKeySpec = SecretKeySpec(secretKey.encodeToByteArray(), ALGORITHM)
        mac.init(secretKeySpec)
        val hashBytes = mac.doFinal(data.encodeToByteArray())
        val signature = Base64.getUrlEncoder().encodeToString(
            hashBytes.joinToString("") { HEXADECIMAL_FORMATTER.format(it) }
                .encodeToByteArray()
        )
        log.debug("Generated HMAC signature: $signature")
        return signature
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
