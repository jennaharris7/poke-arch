package com.fatbird.pokearch.core.network

import com.fatbird.pokearch.core.network.di.NetworkModule
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Creates a Retrofit [T] pointed at this server, using the app's real [kotlinx.serialization.json.Json]
 * configuration so tests exercise production parsing.
 */
internal inline fun <reified T> MockWebServer.createApi(): T = Retrofit.Builder()
    .baseUrl(url("/"))
    .client(OkHttpClient())
    .addConverterFactory(
        NetworkModule.providesNetworkJson().asConverterFactory("application/json".toMediaType()),
    )
    .build()
    .create(T::class.java)

internal fun MockWebServer.enqueueJson(body: String, code: Int = 200) = enqueue(
    MockResponse.Builder()
        .code(code)
        .addHeader("Content-Type", "application/json")
        .body(body)
        .build(),
)
