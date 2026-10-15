package com.fatbird.pokearch.core.data.model

import com.fatbird.pokearch.core.database.model.PokemonEntity
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.network.model.NetworkPokemon

fun NetworkPokemon.asEntity(source: ApiSource) = PokemonEntity(
    source = source,
    id = id,
    name = name,
    imageUrl = imageUrl,
)
