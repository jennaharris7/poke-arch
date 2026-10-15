package com.fatbird.pokearch.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator.MediatorResult
import com.fatbird.pokearch.core.database.model.PokemonEntity
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.network.model.NetworkPokemon
import com.fatbird.pokearch.core.testing.TestPokemonDao
import com.fatbird.pokearch.core.testing.TestPokemonNetworkDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalPagingApi::class)
class PokemonRemoteMediatorTest {

    private val networkPokemon = (1..50).map { id ->
        NetworkPokemon(id = id, name = "pokemon-$id", imageUrl = "https://example.com/$id.png")
    }
    private val pagingState = PagingState<Int, PokemonEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 20, initialLoadSize = 30),
        leadingPlaceholderCount = 0,
    )

    private lateinit var pokemonDao: TestPokemonDao
    private lateinit var networkDataSource: TestPokemonNetworkDataSource
    private lateinit var mediator: PokemonRemoteMediator

    @Before
    fun setUp() {
        pokemonDao = TestPokemonDao()
        networkDataSource = TestPokemonNetworkDataSource(networkPokemon)
        mediator = PokemonRemoteMediator(
            source = ApiSource.REST,
            pokemonDao = pokemonDao,
            networkDataSource = networkDataSource,
        )
    }

    @Test
    fun refresh_cachesInitialPageAndNextOffset() = runTest {
        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertIs<MediatorResult.Success>(result)
        assertFalse(result.endOfPaginationReached)
        assertEquals((1..30).toList(), cachedIds())
        assertEquals(30, pokemonDao.getRemoteKeys(ApiSource.REST)?.nextOffset)
    }

    @Test
    fun refresh_replacesPreviouslyCachedPages() = runTest {
        mediator.load(LoadType.REFRESH, pagingState)
        mediator.load(LoadType.APPEND, pagingState)

        mediator.load(LoadType.REFRESH, pagingState)

        assertEquals((1..30).toList(), cachedIds())
    }

    @Test
    fun append_cachesNextPage() = runTest {
        mediator.load(LoadType.REFRESH, pagingState)

        val result = mediator.load(LoadType.APPEND, pagingState)

        assertIs<MediatorResult.Success>(result)
        // Only 20 of 50 remain, so this page is full and the end isn't known yet.
        assertFalse(result.endOfPaginationReached)
        assertEquals((1..50).toList(), cachedIds())
    }

    @Test
    fun append_shortPage_reachesEnd() = runTest {
        mediator.load(LoadType.REFRESH, pagingState)
        mediator.load(LoadType.APPEND, pagingState)

        val result = mediator.load(LoadType.APPEND, pagingState)

        assertIs<MediatorResult.Success>(result)
        assertTrue(result.endOfPaginationReached)
        assertNull(pokemonDao.getRemoteKeys(ApiSource.REST)?.nextOffset)
    }

    @Test
    fun append_beforeRefresh_reachesEndWithoutFetching() = runTest {
        networkDataSource.error = IOException("should not be called")

        val result = mediator.load(LoadType.APPEND, pagingState)

        assertIs<MediatorResult.Success>(result)
        assertTrue(result.endOfPaginationReached)
    }

    @Test
    fun prepend_alwaysReachesEnd() = runTest {
        val result = mediator.load(LoadType.PREPEND, pagingState)

        assertIs<MediatorResult.Success>(result)
        assertTrue(result.endOfPaginationReached)
    }

    @Test
    fun refresh_networkFailure_returnsErrorAndKeepsCache() = runTest {
        mediator.load(LoadType.REFRESH, pagingState)
        networkDataSource.error = IOException("offline")

        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertIs<MediatorResult.Error>(result)
        assertIs<IOException>(result.throwable)
        assertEquals((1..30).toList(), cachedIds())
    }

    private suspend fun cachedIds() =
        pokemonDao.getPokemon(ApiSource.REST).first().map(PokemonEntity::id)
}
