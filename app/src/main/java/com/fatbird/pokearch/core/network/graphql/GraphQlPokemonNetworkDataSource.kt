package com.fatbird.pokearch.core.network.graphql

import com.fatbird.pokearch.core.network.PokemonNetworkDataSource
import com.fatbird.pokearch.core.network.model.NetworkPokemon
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [PokemonNetworkDataSource] backed by the GraphQL PokeAPI wrapper.
 */
@Singleton
internal class GraphQlPokemonNetworkDataSource @Inject constructor(
    private val api: GraphQlPokemonApi,
) : PokemonNetworkDataSource {

    override suspend fun getPokemon(limit: Int, offset: Int): List<NetworkPokemon> {
        val response = api.getPokemons(
            GraphQlRequest(
                query = PokemonQueries.POKEMONS,
                variables = buildJsonObject {
                    put("limit", limit)
                    put("offset", offset)
                },
            ),
        )
        // GraphQL reports failures in the body with a 200 status, so they must be checked explicitly.
        if (!response.errors.isNullOrEmpty()) {
            throw GraphQlException(response.errors)
        }
        val data = response.data ?: throw GraphQlException(emptyList())
        return data.pokemons.results.map(GraphQlPokemonItem::asNetworkPokemon)
    }
}

internal fun GraphQlPokemonItem.asNetworkPokemon() = NetworkPokemon(
    id = id,
    name = name,
    imageUrl = image,
)

/**
 * Thrown when a GraphQL response contains errors or no data.
 */
class GraphQlException internal constructor(
    errors: List<GraphQlError>,
) : RuntimeException(
    errors.joinToString(separator = "; ") { it.message }.ifEmpty { "GraphQL response had no data" },
)
