package com.fatbird.pokearch.core.network.graphql

/**
 * GraphQL documents sent as plain strings, without a code-generating client.
 */
internal object PokemonQueries {
    val POKEMONS = """
        query pokemons(${'$'}limit: Int, ${'$'}offset: Int) {
          pokemons(limit: ${'$'}limit, offset: ${'$'}offset) {
            count
            results {
              id
              name
              image
            }
          }
        }
    """.trimIndent()
}
