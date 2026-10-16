package com.fatbird.pokearch.core.testing

import androidx.paging.PagingData
import com.fatbird.pokearch.core.data.repository.PokemonRepository
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.model.Pokemon
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

/**
 * [PokemonRepository] whose cached data and refresh outcome are controlled by the test.
 */
class TestPokemonRepository : PokemonRepository {

    private val pokemonFlow = MutableSharedFlow<List<Pokemon>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** When set, [refresh] throws it. */
    var refreshError: Throwable? = null

    /** When set, [refresh] suspends until it completes, keeping the refresh in flight. */
    var refreshGate: CompletableDeferred<Unit>? = null

    /** Every source [refresh] was called with, in order. */
    val refreshedSources = mutableListOf<ApiSource>()

    override fun getPokemonPagingData(source: ApiSource): Flow<PagingData<Pokemon>> =
        pokemonFlow.map { PagingData.from(it) }

    override fun getPokemon(source: ApiSource): Flow<List<Pokemon>> = pokemonFlow

    override suspend fun refresh(source: ApiSource) {
        refreshedSources += source
        refreshGate?.await()
        refreshError?.let { throw it }
    }

    /** Emits [pokemon] as the current cache contents. */
    fun sendPokemon(pokemon: List<Pokemon>) {
        pokemonFlow.tryEmit(pokemon)
    }
}
