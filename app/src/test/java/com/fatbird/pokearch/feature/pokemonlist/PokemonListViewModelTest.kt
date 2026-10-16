package com.fatbird.pokearch.feature.pokemonlist

import androidx.paging.testing.asSnapshot
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.model.Pokemon
import com.fatbird.pokearch.core.testing.MainDispatcherRule
import com.fatbird.pokearch.core.testing.TestPokemonRepository
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
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
    fun creation_requestsPagesFromItsApiSource() {
        createViewModel(ApiSource.REST)

        assertEquals(listOf(ApiSource.REST), repository.requestedSources)
    }

    @Test
    fun pokemon_emitsRepositoryPages() = runTest {
        val viewModel = createViewModel()
        repository.sendPokemon(samplePokemon)

        assertEquals(samplePokemon, viewModel.pokemon.asSnapshot())
    }
}
