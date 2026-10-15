package com.fatbird.pokearch.core.testing

import com.fatbird.pokearch.core.network.PokemonNetworkDataSource
import com.fatbird.pokearch.core.network.model.NetworkPokemon

/**
 * [PokemonNetworkDataSource] that serves [pokemon] from memory, or throws [error] when set.
 */
class TestPokemonNetworkDataSource(
    private val pokemon: List<NetworkPokemon> = emptyList(),
) : PokemonNetworkDataSource {

    var error: Throwable? = null

    override suspend fun getPokemon(limit: Int, offset: Int): List<NetworkPokemon> {
        error?.let { throw it }
        return pokemon.drop(offset).take(limit)
    }
}
