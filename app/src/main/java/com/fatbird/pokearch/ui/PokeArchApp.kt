package com.fatbird.pokearch.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.fatbird.pokearch.feature.home.navigation.HomeNavKey
import com.fatbird.pokearch.feature.home.navigation.homeEntry
import com.fatbird.pokearch.feature.pokemonlist.navigation.PokemonListNavKey
import com.fatbird.pokearch.feature.pokemonlist.navigation.pokemonListEntry

/**
 * Root of the app UI. Owns the Navigation 3 back stack, which survives configuration changes and
 * process death because every key is serializable.
 */
@Composable
fun PokeArchApp(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(HomeNavKey)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            // Scopes each entry's ViewModels to its time on the back stack.
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            homeEntry(
                onApiSourceClick = { apiSource -> backStack.add(PokemonListNavKey(apiSource)) },
            )
            pokemonListEntry(
                onBackClick = { backStack.removeLastOrNull() },
            )
        },
    )
}
