package com.fatbird.pokearch.core.network.model

/**
 * Network representation of a Pokémon, shared by every [com.fatbird.pokearch.core.network.PokemonNetworkDataSource].
 */
data class NetworkPokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
)
