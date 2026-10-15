package com.fatbird.pokearch.feature.pokemonlist.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.feature.pokemonlist.PokemonListRoute
import kotlinx.serialization.Serializable

@Serializable
data class PokemonListNavKey(val apiSource: ApiSource) : NavKey

fun EntryProviderScope<NavKey>.pokemonListEntry(
    onBackClick: () -> Unit,
) {
    entry<PokemonListNavKey> { key ->
        PokemonListRoute(
            apiSource = key.apiSource,
            onBackClick = onBackClick,
        )
    }
}
