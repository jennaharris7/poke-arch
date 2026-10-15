package com.fatbird.pokearch.core.network.rest

import kotlinx.serialization.Serializable

/**
 * Response body of `GET /pokemon`.
 */
@Serializable
internal data class RestPokemonListResponse(
    val count: Int,
    val next: String? = null,
    val previous: String? = null,
    val results: List<RestNamedResource>,
)

/**
 * A named reference to another resource, e.g. `{"name": "bulbasaur", "url": ".../pokemon/1/"}`.
 */
@Serializable
internal data class RestNamedResource(
    val name: String,
    val url: String,
)
