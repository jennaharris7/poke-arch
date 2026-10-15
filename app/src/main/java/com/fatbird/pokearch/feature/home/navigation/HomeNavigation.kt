package com.fatbird.pokearch.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.feature.home.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
data object HomeNavKey : NavKey

fun EntryProviderScope<NavKey>.homeEntry(
    onApiSourceClick: (ApiSource) -> Unit,
) {
    entry<HomeNavKey> {
        HomeScreen(onApiSourceClick = onApiSourceClick)
    }
}
