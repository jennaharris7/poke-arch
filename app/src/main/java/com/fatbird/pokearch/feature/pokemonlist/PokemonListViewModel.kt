package com.fatbird.pokearch.feature.pokemonlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.fatbird.pokearch.core.data.repository.PokemonRepository
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.model.Pokemon
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow

@HiltViewModel(assistedFactory = PokemonListViewModel.Factory::class)
class PokemonListViewModel @AssistedInject constructor(
    @Assisted apiSource: ApiSource,
    pokemonRepository: PokemonRepository,
) : ViewModel() {

    /**
     * Pokémon from the selected API, cached in the ViewModel so loaded pages survive configuration
     * changes. Loading, refresh and error states travel with the paging data as load states.
     */
    val pokemon: Flow<PagingData<Pokemon>> = pokemonRepository
        .getPokemonPagingData(apiSource)
        .cachedIn(viewModelScope)

    @AssistedFactory
    interface Factory {
        fun create(apiSource: ApiSource): PokemonListViewModel
    }
}
