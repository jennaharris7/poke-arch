package com.fatbird.pokearch.core.network.graphql

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API declaration for the GraphQL PokeAPI endpoint. Every operation is a POST to the
 * same URL, distinguished only by the query document in the body.
 */
internal interface GraphQlPokemonApi {
    @POST(".")
    suspend fun getPokemons(@Body request: GraphQlRequest): GraphQlResponse<GraphQlPokemonsData>
}
