# Rapyd API Headers Kotlin Library

## Overview

The `rapyd-api-headers` library provides functionality for generating headers required for Rapyd API requests. It includes methods for creating HMAC SHA-256 signatures and other necessary headers.

**IMPORTANT:** Never share your access key and secret key publicly or with unauthorized personnel. Additionally, avoid enabling debug logging in production environments, as it may expose sensitive information.

## Features

- **Generate API Headers:** Automatically generate the headers required for Rapyd API calls, including the HMAC signature, salt, and timestamp.
- **Support for Various HTTP Methods:** Handles different HTTP methods (GET, POST, etc.).
- **Customizable:** Allows customization of request bodies and paths, and enables overriding the default JSON ObjectMapper.

## Compatibility

This library requires Java 17 or higher.

## License

This project is licensed under the Apache License 2.0. See the LICENSE file for details.
Feel free to adjust any part of the content to better fit your needs!


## Usage

### Adding to Your Project

Add the following dependency to your `build.gradle` file:

```kotlin
dependencies {
    implementation("com.smelman:rapyd-api-headers:1.0.0")
}
```

## Kotlin

```kotlin
import com.fasterxml.jackson.databind.ObjectMapper
import com.smelman.rapyd.api.headers.service.HeadersServiceImpl

fun main() {
    val accessKey = "your_access_key"
    val secretKey = "your_secret_key"

    val headersService = HeadersServiceImpl(accessKey, secretKey)

    // Example for GET request without body
    val headers = headersService.generateRapydHeaders(
        httpMethod = "GET",
        path = "/v1/data/countries"
    )

    println(headers.toString())

    // Example for POST request with body
    val requestBody = mapOf("key" to "exampleKey", "value" to "exampleValue")
    val headersWithBody = headersService.generateRapydHeaders(
        httpMethod = "POST",
        path = "/v1/data/countries",
        body = requestBody
    )

    println(headersWithBody.toString())
}
```

## Java

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smelman.rapyd.api.headers.service.HeadersServiceImpl;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        String accessKey = "your_access_key";
        String secretKey = "your_secret_key";

        HeadersServiceImpl headersService = new HeadersServiceImpl(accessKey, secretKey);
        
        // Example for GET request without body
        var headers = headersService.generateRapydHeaders(
            "GET",
            "/v1/data/countries"
        );
        
        System.out.println(headers.toString());

        // Example for POST request with body
        Map<String, Object> body = new HashMap<>();
        body.put("key", "exampleKey");
        body.put("value", "exampleValue");

        var headersWithBody = headersService.generateRapydHeaders(
            "POST",
            "/v1/data/countries",
            body
        );
        
        System.out.println(headersWithBody.toString());
    }
}
```

## Groovy

```groovy
@Grab(group='com.smelman', module='rapyd-api-headers', version='1.0.0')
import com.fasterxml.jackson.databind.ObjectMapper
import com.smelman.rapyd.api.headers.service.HeadersServiceImpl

def accessKey = "your_access_key"
def secretKey = "your_secret_key"

def headersService = new HeadersServiceImpl(accessKey, secretKey)

// Example for GET request without body
def headers = headersService.generateRapydHeaders(
    "GET",
    "/v1/data/countries"
)

println(headers)

// Example for POST request with body
def body = [key: 'exampleKey', value: 'exampleValue']

def headersWithBody = headersService.generateRapydHeaders(
    "POST",
    "/v1/data/countries",
    body
)

println(headersWithBody)
```