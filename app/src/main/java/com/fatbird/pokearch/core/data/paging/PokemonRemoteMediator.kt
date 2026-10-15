package com.fatbird.pokearch.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.fatbird.pokearch.core.data.model.asEntity
import com.fatbird.pokearch.core.database.dao.PokemonDao
import com.fatbird.pokearch.core.database.model.PokemonEntity
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.network.PokemonNetworkDataSource
import kotlinx.coroutines.CancellationException

/**
 * Fills the Room cache for [source] page by page from [networkDataSource]. The UI only ever reads
 * from Room, so if a request fails whatever is already cached stays on screen.
 */
@OptIn(ExperimentalPagingApi::class)
internal class PokemonRemoteMediator(
    private val source: ApiSource,
    private val pokemonDao: PokemonDao,
    private val networkDataSource: PokemonNetworkDataSource,
) : RemoteMediator<Int, PokemonEntity>() {

    /** Always check for fresh data when the list is opened, falling back to the cache on error. */
    override suspend fun initialize(): InitializeAction = InitializeAction.LAUNCH_INITIAL_REFRESH

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonEntity>,
    ): MediatorResult {
        val offset = when (loadType) {
            LoadType.REFRESH -> 0
            // The list always starts at offset 0, so there is nothing before it.
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            // No key means nothing was ever loaded, and a null offset means the end was reached.
            LoadType.APPEND -> pokemonDao.getRemoteKeys(source)?.nextOffset
                ?: return MediatorResult.Success(endOfPaginationReached = true)
        }
        val limit = when (loadType) {
            LoadType.REFRESH -> state.config.initialLoadSize
            else -> state.config.pageSize
        }

        return try {
            val page = networkDataSource.getPokemon(limit = limit, offset = offset)
            // Neither API reports a next page directly, so a short page marks the end.
            val endOfPaginationReached = page.size < limit
            val nextOffset = if (endOfPaginationReached) null else offset + page.size
            val entities = page.map { it.asEntity(source) }
            if (loadType == LoadType.REFRESH) {
                pokemonDao.replacePokemon(source, entities, nextOffset)
            } else {
                pokemonDao.appendPokemon(source, entities, nextOffset)
            }
            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
