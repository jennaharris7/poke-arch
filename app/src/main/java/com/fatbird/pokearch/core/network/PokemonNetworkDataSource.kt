package com.fatbird.pokearch.core.network

import com.fatbird.pokearch.core.network.model.NetworkPokemon

/**
 * Fetches Pokémon from a remote API. Each [com.fatbird.pokearch.core.model.ApiSource] has its own
 * implementation, normalizing its API-specific payloads into [NetworkPokemon].
 */
interface PokemonNetworkDataSource {
    suspend fun getPokemon(limit: Int, offset: Int): List<NetworkPokemon>
}
