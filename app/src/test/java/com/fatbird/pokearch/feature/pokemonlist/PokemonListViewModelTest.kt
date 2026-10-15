package com.fatbird.pokearch.feature.pokemonlist

import app.cash.turbine.test
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.model.Pokemon
import com.fatbird.pokearch.core.testing.MainDispatcherRule
import com.fatbird.pokearch.core.testing.TestPokemonRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals

class PokemonListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestPokemonRepository()

    private val samplePokemon = listOf(
        Pokemon(id = 1, name = "bulbasaur", imageUrl = "https://example.com/1.png"),
        Pokemon(id = 25, name = "pikachu", imageUrl = "https://example.com/25.png"),
    )

    private fun createViewModel(apiSource: ApiSource = ApiSource.GRAPHQL) =
        PokemonListViewModel(apiSource = apiSource, pokemonRepository = repository)

    @Test
    fun uiState_initially_isLoading() {
        val viewModel = createViewModel()

        assertEquals(PokemonListUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun creation_refreshesItsApiSource() {
        createViewModel(ApiSource.REST)

        assertEquals(listOf(ApiSource.REST), repository.refreshedSources)
    }

    @Test
    fun uiState_whenCacheHasPokemon_isSuccess() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(PokemonListUiState.Loading, awaitItem())
            repository.sendPokemon(samplePokemon)
            assertEquals(PokemonListUiState.Success(samplePokemon), awaitItem())
        }
    }

    @Test
    fun uiState_whileRefreshingWithCache_showsRefreshing() = runTest {
        val refreshGate = CompletableDeferred<Unit>()
        repository.refreshGate = refreshGate
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(PokemonListUiState.Loading, awaitItem())
            repository.sendPokemon(samplePokemon)
            assertEquals(
                PokemonListUiState.Success(samplePokemon, isRefreshing = true),
                awaitItem(),
            )
            refreshGate.complete(Unit)
            assertEquals(PokemonListUiState.Success(samplePokemon), awaitItem())
        }
    }

    @Test
    fun uiState_whenRefreshFailsWithEmptyCache_isError() = runTest {
        repository.refreshError = IOException("offline")
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(PokemonListUiState.Loading, awaitItem())
            repository.sendPokemon(emptyList())
            assertEquals(PokemonListUiState.Error, awaitItem())
        }
    }

    @Test
    fun uiState_whenRefreshFailsWithCache_isSuccessWithRefreshFailed() = runTest {
        repository.refreshError = IOException("offline")
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(PokemonListUiState.Loading, awaitItem())
            repository.sendPokemon(samplePokemon)
            assertEquals(
                PokemonListUiState.Success(samplePokemon, refreshFailed = true),
                awaitItem(),
            )
        }
    }

    @Test
    fun refresh_afterError_recoversToSuccess() = runTest {
        repository.refreshError = IOException("offline")
        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertEquals(PokemonListUiState.Loading, awaitItem())
            repository.sendPokemon(emptyList())
            assertEquals(PokemonListUiState.Error, awaitItem())

            repository.refreshError = null
            val refreshGate = CompletableDeferred<Unit>()
            repository.refreshGate = refreshGate
            viewModel.refresh()
            assertEquals(PokemonListUiState.Loading, awaitItem())

            repository.sendPokemon(samplePokemon)
            assertEquals(
                PokemonListUiState.Success(samplePokemon, isRefreshing = true),
                awaitItem(),
            )
            refreshGate.complete(Unit)
            assertEquals(PokemonListUiState.Success(samplePokemon), awaitItem())
        }
    }

    @Test
    fun refresh_whileRefreshInFlight_isIgnored() {
        repository.refreshGate = CompletableDeferred()
        val viewModel = createViewModel()

        viewModel.refresh()

        assertEquals(1, repository.refreshedSources.size)
    }
}
