package com.fatbird.pokearch.core.network.rest

import com.fatbird.pokearch.core.network.PokemonNetworkDataSource
import com.fatbird.pokearch.core.network.model.NetworkPokemon
import javax.inject.Inject
import javax.inject.Singleton

private const val SPRITE_BASE_URL =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon"

/**
 * [PokemonNetworkDataSource] backed by the PokeAPI REST API.
 */
@Singleton
internal class RestPokemonNetworkDataSource @Inject constructor(
    private val api: RestPokemonApi,
) : PokemonNetworkDataSource {

    override suspend fun getPokemon(limit: Int, offset: Int): List<NetworkPokemon> =
        api.getPokemonList(limit = limit, offset = offset).results.map(RestNamedResource::asNetworkPokemon)
}

/**
 * The list endpoint only returns a name and a resource URL, so the id is parsed from the URL
 * (e.g. `.../pokemon/25/`) and the sprite URL is derived from the id.
 */
internal fun RestNamedResource.asNetworkPokemon(): NetworkPokemon {
    val id = url.trimEnd('/').substringAfterLast('/').toInt()
    return NetworkPokemon(
        id = id,
        name = name,
        imageUrl = "$SPRITE_BASE_URL/$id.png",
    )
}
