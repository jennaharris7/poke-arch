package com.fatbird.pokearch.core.data.repository

import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.model.Pokemon
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {

    /**
     * Observes the locally cached Pokémon fetched from [source].
     */
    fun getPokemon(source: ApiSource): Flow<List<Pokemon>>

    /**
     * Fetches the latest Pokémon from [source] and writes them to the local cache, which in turn
     * emits through [getPokemon]. Throws if the network request fails.
     */
    suspend fun refresh(source: ApiSource)
}
