package com.fatbird.pokearch.core.data.repository

import androidx.paging.PagingData
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.model.Pokemon
import kotlinx.coroutines.flow.Flow

interface PokemonRepository {

    /**
     * Pages through the Pokémon from [source]. Pages are read from the local cache, which is
     * refreshed when collection starts and filled from the network as the list is scrolled.
     */
    fun getPokemonPagingData(source: ApiSource): Flow<PagingData<Pokemon>>

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
