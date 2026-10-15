package com.fatbird.pokearch.feature.pokemonlist

import com.fatbird.pokearch.core.model.Pokemon

sealed interface PokemonListUiState {

    /** Nothing cached yet and the first refresh is in flight. */
    data object Loading : PokemonListUiState

    /** Nothing cached and the refresh failed. */
    data object Error : PokemonListUiState

    /**
     * Cached Pokémon are available.
     *
     * @param isRefreshing a refresh is in flight.
     * @param refreshFailed the latest refresh failed, so [pokemon] may be stale.
     */
    data class Success(
        val pokemon: List<Pokemon>,
        val isRefreshing: Boolean = false,
        val refreshFailed: Boolean = false,
    ) : PokemonListUiState
}
