package com.fatbird.pokearch.core.data.repository

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
