package com.fatbird.pokearch.core.model

/**
 * A Pokémon as presented to the rest of the app, independent of which API it came from.
 */
data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
)
