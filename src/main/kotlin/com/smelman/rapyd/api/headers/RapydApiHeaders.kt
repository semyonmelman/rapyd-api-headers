package com.smelman.rapyd.api.headers

import com.fasterxml.jackson.databind.ObjectMapper
import com.smelman.rapyd.api.headers.service.HeadersService

/**
 * A class for generating Rapyd API headers using the [HeadersService] class.
 *
 * This class provides methods to generate the required headers for making Rapyd API requests.
 * It uses the [HeadersService] to handle the creation of the necessary headers, including the HMAC signature.
 *
 * **IMPORTANT:** Never share your access key and secret key with unauthorized individuals or services.
 *
 * @param accessKey The access key for the Rapyd API.
 * @param secretKey The secret key for the Rapyd API.
 * @param objectMapper An optional [ObjectMapper] instance for JSON processing. If not provided,
 *                     a default [ObjectMapper] instance will be used.
 */
class RapydApiHeaders internal constructor(
    private val headersService: HeadersService
) {

    constructor(accessKey: String, secretKey: String, objectMapper: ObjectMapper = ObjectMapper()) :
            this(HeadersService(accessKey, secretKey, objectMapper))

    /**
     * Generates the necessary headers for a Rapyd API call, including the HMAC signature.
     *
     * @param httpMethod The HTTP method (e.g., "GET", "POST").
     * @param path The request path (e.g., "/v1/data/countries"). For more information, see the Rapyd API documentation.
     * @param body Optional request body.
     * @return A map of headers including the access key, salt, timestamp, and signature.
     */
    fun <T> generateHeaders(httpMethod: String, path: String, body: T? = null): Map<String, String> {
        return headersService.generateRapydHeaders(
            httpMethod, path, body
        )
    }

    /**
     * Generates the necessary headers for a Rapyd API call, including the HMAC signature and additional headers.
     *
     * @param httpMethod The HTTP method (e.g., "GET", "POST").
     * @param path The request path (e.g., "/v1/data/countries"). For more information, see the Rapyd API documentation.
     * @param body Optional request body.
     * @param additionalHeaders Additional headers to include in the final headers map.
     * @return A map of headers including the access key, salt, timestamp, signature, and any additional headers.
     */
    fun <T> generateRapydHeaders(
        httpMethod: String,
        path: String,
        body: T? = null,
        additionalHeaders: Map<String, String> = emptyMap()
    ): Map<String, String> {
        val headers = headersService.generateRapydHeaders(httpMethod, path, body)
        return additionalHeaders + headers
    }

    /**
     * Generates the necessary headers for a Rapyd API call, including the HMAC signature,
     * additional headers, and custom salt and timestamp.
     *
     * @param httpMethod The HTTP method (e.g., "GET", "POST").
     * @param path The request path (e.g., "/v1/data/countries"). For more information, see the Rapyd API documentation.
     * @param body Optional request body.
     * @param additionalHeaders Additional headers to include in the final headers map.
     * @param timestamp Optional custom timestamp.
     * @param salt Optional custom salt.
     * @return A map of headers including the access key, custom salt, custom timestamp, signature, and any additional headers.
     */
    fun <T> generateRapydHeaders(
        httpMethod: String,
        path: String,
        body: T? = null,
        additionalHeaders: Map<String, String> = emptyMap(),
        timestamp: Long? = null,
        salt: String? = null
    ): Map<String, String> {
        val headers = headersService.generateRapydHeaders(httpMethod, path, body, salt, timestamp)
        return additionalHeaders + headers
    }
}