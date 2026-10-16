package com.fatbird.pokearch.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.fatbird.pokearch.core.data.model.asEntity
import com.fatbird.pokearch.core.data.paging.PokemonRemoteMediator
import com.fatbird.pokearch.core.database.dao.PokemonDao
import com.fatbird.pokearch.core.database.model.PokemonEntity
import com.fatbird.pokearch.core.database.model.asExternalModel
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.model.Pokemon
import com.fatbird.pokearch.core.network.PokemonNetworkDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Original 151 Pokémon. Loaded as a single fixed page until paging is introduced.
 */
private const val FIRST_PAGE_SIZE = 151

private const val PAGE_SIZE = 50

/**
 * [PokemonRepository] that treats the Room database as the single source of truth: readers only
 * observe the database, and [refresh] updates it from the network.
 */
internal class OfflineFirstPokemonRepository @Inject constructor(
    private val pokemonDao: PokemonDao,
    private val networkDataSources: Map<ApiSource, @JvmSuppressWildcards PokemonNetworkDataSource>,
) : PokemonRepository {

    @OptIn(ExperimentalPagingApi::class)
    override fun getPokemonPagingData(source: ApiSource): Flow<PagingData<Pokemon>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE),
        remoteMediator = PokemonRemoteMediator(
            source = source,
            pokemonDao = pokemonDao,
            networkDataSource = networkDataSources.getValue(source),
        ),
        pagingSourceFactory = { pokemonDao.pagingSource(source) },
    ).flow.map { pagingData ->
        pagingData.map(PokemonEntity::asExternalModel)
    }

    override fun getPokemon(source: ApiSource): Flow<List<Pokemon>> =
        pokemonDao.getPokemon(source).map { entities ->
            entities.map(PokemonEntity::asExternalModel)
        }

    override suspend fun refresh(source: ApiSource) {
        val networkPokemon = networkDataSources.getValue(source)
            .getPokemon(limit = FIRST_PAGE_SIZE, offset = 0)
        pokemonDao.upsertPokemon(networkPokemon.map { it.asEntity(source) })
    }
}
