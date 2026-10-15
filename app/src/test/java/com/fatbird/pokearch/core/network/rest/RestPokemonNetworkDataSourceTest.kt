package com.fatbird.pokearch.core.network.rest

import com.fatbird.pokearch.core.network.createApi
import com.fatbird.pokearch.core.network.enqueueJson
import com.fatbird.pokearch.core.network.model.NetworkPokemon
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RestPokemonNetworkDataSourceTest {

    private val server = MockWebServer()
    private lateinit var dataSource: RestPokemonNetworkDataSource

    @Before
    fun setUp() {
        server.start()
        dataSource = RestPokemonNetworkDataSource(server.createApi())
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun getPokemon_requestsListWithLimitAndOffset() = runTest {
        server.enqueueJson(POKEMON_LIST_JSON)

        dataSource.getPokemon(limit = 2, offset = 24)

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/pokemon", request.url.encodedPath)
        assertEquals("2", request.url.queryParameter("limit"))
        assertEquals("24", request.url.queryParameter("offset"))
    }

    @Test
    fun getPokemon_derivesIdAndSpriteFromResourceUrl() = runTest {
        server.enqueueJson(POKEMON_LIST_JSON)

        val pokemon = dataSource.getPokemon(limit = 2, offset = 24)

        assertEquals(
            listOf(
                NetworkPokemon(
                    id = 25,
                    name = "pikachu",
                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png",
                ),
                NetworkPokemon(
                    id = 26,
                    name = "raichu",
                    imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/26.png",
                ),
            ),
            pokemon,
        )
    }

    @Test
    fun getPokemon_httpError_throws() = runTest {
        server.enqueueJson("""{"detail": "Not found."}""", code = 404)

        assertFailsWith<HttpException> {
            dataSource.getPokemon(limit = 2, offset = 0)
        }
    }
}

private val POKEMON_LIST_JSON = """
    {
      "count": 1351,
      "next": "https://pokeapi.co/api/v2/pokemon?offset=26&limit=2",
      "previous": "https://pokeapi.co/api/v2/pokemon?offset=22&limit=2",
      "results": [
        { "name": "pikachu", "url": "https://pokeapi.co/api/v2/pokemon/25/" },
        { "name": "raichu", "url": "https://pokeapi.co/api/v2/pokemon/26/" }
      ]
    }
""".trimIndent()
