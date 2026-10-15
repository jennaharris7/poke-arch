package com.fatbird.pokearch.feature.pokemonlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fatbird.pokearch.core.data.repository.PokemonRepository
import com.fatbird.pokearch.core.model.ApiSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = PokemonListViewModel.Factory::class)
class PokemonListViewModel @AssistedInject constructor(
    @Assisted private val apiSource: ApiSource,
    private val pokemonRepository: PokemonRepository,
) : ViewModel() {

    private val refreshState = MutableStateFlow(RefreshState.Refreshing)
    private var refreshJob: Job? = null

    val uiState: StateFlow<PokemonListUiState> = combine(
        pokemonRepository.getPokemon(apiSource),
        refreshState,
    ) { pokemon, refreshState ->
        when {
            pokemon.isNotEmpty() -> PokemonListUiState.Success(
                pokemon = pokemon,
                isRefreshing = refreshState == RefreshState.Refreshing,
                refreshFailed = refreshState == RefreshState.Failed,
            )
            refreshState == RefreshState.Failed -> PokemonListUiState.Error
            else -> PokemonListUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PokemonListUiState.Loading,
    )

    init {
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            refreshState.value = RefreshState.Refreshing
            refreshState.value = try {
                pokemonRepository.refresh(apiSource)
                RefreshState.Idle
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                RefreshState.Failed
            }
        }
    }

    private enum class RefreshState { Idle, Refreshing, Failed }

    @AssistedFactory
    interface Factory {
        fun create(apiSource: ApiSource): PokemonListViewModel
    }
}
