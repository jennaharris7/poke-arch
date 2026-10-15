package com.fatbird.pokearch.core.network.di

import android.content.Context
import coil3.ImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.fatbird.pokearch.BuildConfig
import com.fatbird.pokearch.core.network.graphql.GraphQlPokemonApi
import com.fatbird.pokearch.core.network.rest.RestPokemonApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object NetworkModule {

    @Provides
    @Singleton
    fun providesNetworkJson(): Json = Json {
        ignoreUnknownKeys = true
    }

    /**
     * Exposed as [Call.Factory] rather than [OkHttpClient] so consumers depend on the
     * narrowest type they need.
     */
    @Provides
    @Singleton
    fun okHttpCallFactory(): Call.Factory = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                if (BuildConfig.DEBUG) {
                    setLevel(HttpLoggingInterceptor.Level.BODY)
                }
            },
        )
        .build()

    @Provides
    @Singleton
    fun providesRestPokemonApi(
        networkJson: Json,
        okhttpCallFactory: dagger.Lazy<Call.Factory>,
    ): RestPokemonApi = retrofit(BuildConfig.POKE_API_REST_URL, networkJson, okhttpCallFactory)
        .create(RestPokemonApi::class.java)

    @Provides
    @Singleton
    fun providesGraphQlPokemonApi(
        networkJson: Json,
        okhttpCallFactory: dagger.Lazy<Call.Factory>,
    ): GraphQlPokemonApi = retrofit(BuildConfig.POKE_API_GRAPHQL_URL, networkJson, okhttpCallFactory)
        .create(GraphQlPokemonApi::class.java)

    private fun retrofit(
        baseUrl: String,
        networkJson: Json,
        okhttpCallFactory: dagger.Lazy<Call.Factory>,
    ): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        // Defer OkHttp creation until the first request so it never happens on the main thread.
        .callFactory { okhttpCallFactory.get().newCall(it) }
        .addConverterFactory(networkJson.asConverterFactory("application/json".toMediaType()))
        .build()

    /**
     * Coil image loader that shares the app's OkHttp client. Installed as Coil's singleton by
     * [com.fatbird.pokearch.PokeArchApplication].
     */
    @Provides
    @Singleton
    fun imageLoader(
        okHttpCallFactory: dagger.Lazy<Call.Factory>,
        @ApplicationContext application: Context,
    ): ImageLoader = ImageLoader.Builder(application)
        .components {
            add(OkHttpNetworkFetcherFactory(callFactory = { okHttpCallFactory.get() }))
        }
        .crossfade(true)
        .build()
}
