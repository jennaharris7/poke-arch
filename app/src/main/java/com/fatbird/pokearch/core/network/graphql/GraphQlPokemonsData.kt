package com.fatbird.pokearch.core.network.graphql

import kotlinx.serialization.Serializable

/**
 * `data` payload of [PokemonQueries.POKEMONS].
 */
@Serializable
internal data class GraphQlPokemonsData(
    val pokemons: GraphQlPokemonList,
)

@Serializable
internal data class GraphQlPokemonList(
    val count: Int,
    val results: List<GraphQlPokemonItem>,
)

@Serializable
internal data class GraphQlPokemonItem(
    val id: Int,
    val name: String,
    val image: String,
)
