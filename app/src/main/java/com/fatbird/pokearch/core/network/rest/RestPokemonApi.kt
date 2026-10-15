package com.fatbird.pokearch.core.network.rest

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API declaration for the PokeAPI REST endpoints.
 */
internal interface RestPokemonApi {
    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): RestPokemonListResponse
}
