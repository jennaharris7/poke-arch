package com.fatbird.pokearch.core.model

/**
 * The PokeAPI flavor used to fetch data.
 */
enum class ApiSource {
    /** https://pokeapi.co REST API. */
    REST,

    /** https://github.com/mazipan/graphql-pokeapi GraphQL wrapper. */
    GRAPHQL,
}
