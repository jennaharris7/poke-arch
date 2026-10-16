package com.fatbird.pokearch.core.data.repository

import androidx.paging.testing.asSnapshot
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.model.Pokemon
import com.fatbird.pokearch.core.network.model.NetworkPokemon
import com.fatbird.pokearch.core.testing.TestPokemonDao
import com.fatbird.pokearch.core.testing.TestPokemonNetworkDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class OfflineFirstPokemonRepositoryTest {

    private val restPokemon = (1..200).map { id ->
        NetworkPokemon(id = id, name = "rest-$id", imageUrl = "https://example.com/rest/$id.png")
    }
    private val graphQlPokemon = (1..200).map { id ->
        NetworkPokemon(id = id, name = "graphql-$id", imageUrl = "https://example.com/graphql/$id.png")
    }

    private lateinit var restDataSource: TestPokemonNetworkDataSource
    private lateinit var graphQlDataSource: TestPokemonNetworkDataSource
    private lateinit var repository: OfflineFirstPokemonRepository

    @Before
    fun setUp() {
        restDataSource = TestPokemonNetworkDataSource(restPokemon)
        graphQlDataSource = TestPokemonNetworkDataSource(graphQlPokemon)
        repository = OfflineFirstPokemonRepository(
            pokemonDao = TestPokemonDao(),
            networkDataSources = mapOf(
                ApiSource.REST to restDataSource,
                ApiSource.GRAPHQL to graphQlDataSource,
            ),
        )
    }

    @Test
    fun getPokemonPagingData_loadsInitialPagesFromNetwork() = runTest {
        val snapshot = repository.getPokemonPagingData(ApiSource.REST).asSnapshot()

        assertEquals((1..150).toList(), snapshot.map(Pokemon::id))
        assertEquals(
            Pokemon(id = 25, name = "rest-25", imageUrl = "https://example.com/rest/25.png"),
            snapshot[24],
        )
    }

    @Test
    fun getPokemonPagingData_scrollingToEnd_loadsRemainingPages() = runTest {
        val snapshot = repository.getPokemonPagingData(ApiSource.REST).asSnapshot {
            appendScrollWhile { true }
        }

        assertEquals(200, snapshot.last().id)
        // Each page write invalidates the paging source, which then reloads around the scroll
        // position, so the snapshot is only a window; the cache holds the full list.
        assertEquals((1..200).toList(), repository.getPokemon(ApiSource.REST).first().map(Pokemon::id))
    }

    @Test
    fun getPokemonPagingData_keepsEachSourceSeparate() = runTest {
        val snapshot = repository.getPokemonPagingData(ApiSource.GRAPHQL).asSnapshot()

        assertTrue(snapshot.all { it.name.startsWith("graphql-") })
        assertTrue(repository.getPokemon(ApiSource.REST).first().isEmpty())
    }

    @Test
    fun getPokemonPagingData_networkFailure_surfacesErrorAndKeepsCache() = runTest {
        repository.getPokemonPagingData(ApiSource.REST).asSnapshot()
        val cachedBefore = repository.getPokemon(ApiSource.REST).first()
        restDataSource.error = IOException("offline")

        // asSnapshot rethrows the mediator's error, which reaches the UI as a LoadState.Error.
        assertFailsWith<IOException> {
            repository.getPokemonPagingData(ApiSource.REST).asSnapshot()
        }
        assertEquals(cachedBefore, repository.getPokemon(ApiSource.REST).first())
    }

    @Test
    fun getPokemon_beforeRefresh_isEmpty() = runTest {
        assertTrue(repository.getPokemon(ApiSource.REST).first().isEmpty())
    }

    @Test
    fun refresh_cachesFirstPageFromSelectedSource() = runTest {
        repository.refresh(ApiSource.REST)

        val cached = repository.getPokemon(ApiSource.REST).first()
        assertEquals((1..151).toList(), cached.map(Pokemon::id))
        assertEquals(
            Pokemon(id = 25, name = "rest-25", imageUrl = "https://example.com/rest/25.png"),
            cached[24],
        )
    }

    @Test
    fun refresh_keepsEachSourceCacheSeparate() = runTest {
        repository.refresh(ApiSource.GRAPHQL)

        assertTrue(repository.getPokemon(ApiSource.REST).first().isEmpty())
        assertTrue(
            repository.getPokemon(ApiSource.GRAPHQL).first().all { it.name.startsWith("graphql-") },
        )
    }

    @Test
    fun refresh_networkFailure_throwsAndKeepsCache() = runTest {
        repository.refresh(ApiSource.REST)
        val cachedBefore = repository.getPokemon(ApiSource.REST).first()

        restDataSource.error = IOException("offline")

        assertFailsWith<IOException> { repository.refresh(ApiSource.REST) }
        assertEquals(cachedBefore, repository.getPokemon(ApiSource.REST).first())
    }
}
