package com.fatbird.pokearch.core.testing

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.testing.asPagingSourceFactory
import com.fatbird.pokearch.core.data.repository.PokemonRepository
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.model.Pokemon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/**
 * [PokemonRepository] that pages through Pokémon supplied by the test. A real [Pager] drives the
 * paging so collectors see the same load states as in the app.
 */
class TestPokemonRepository : PokemonRepository {

    private var pokemon = emptyList<Pokemon>()

    /** Every source [getPokemonPagingData] was called with, in order. */
    val requestedSources = mutableListOf<ApiSource>()

    override fun getPokemonPagingData(source: ApiSource): Flow<PagingData<Pokemon>> {
        requestedSources += source
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { pokemon.asPagingSourceFactory().invoke() },
        ).flow
    }

    override fun getPokemon(source: ApiSource): Flow<List<Pokemon>> = emptyFlow()

    override suspend fun refresh(source: ApiSource) = Unit

    /** Sets the Pokémon served by paging sources created from now on. */
    fun sendPokemon(pokemon: List<Pokemon>) {
        this.pokemon = pokemon
    }
}
