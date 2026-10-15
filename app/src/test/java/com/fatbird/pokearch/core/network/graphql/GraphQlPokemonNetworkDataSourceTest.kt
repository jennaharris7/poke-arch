package com.fatbird.pokearch.core.network.graphql

import com.fatbird.pokearch.core.network.createApi
import com.fatbird.pokearch.core.network.enqueueJson
import com.fatbird.pokearch.core.network.model.NetworkPokemon
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GraphQlPokemonNetworkDataSourceTest {

    private val server = MockWebServer()
    private lateinit var dataSource: GraphQlPokemonNetworkDataSource

    @Before
    fun setUp() {
        server.start()
        dataSource = GraphQlPokemonNetworkDataSource(server.createApi())
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun getPokemon_postsQueryAndVariables() = runTest {
        server.enqueueJson(POKEMONS_JSON)

        dataSource.getPokemon(limit = 2, offset = 24)

        val request = server.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/", request.url.encodedPath)
        val body = Json.parseToJsonElement(request.body!!.utf8()).jsonObject
        assertEquals(PokemonQueries.POKEMONS, body.getValue("query").jsonPrimitive.content)
        val variables = body.getValue("variables").jsonObject
        assertEquals(2, variables.getValue("limit").jsonPrimitive.int)
        assertEquals(24, variables.getValue("offset").jsonPrimitive.int)
    }

    @Test
    fun getPokemon_mapsResults() = runTest {
        server.enqueueJson(POKEMONS_JSON)

        val pokemon = dataSource.getPokemon(limit = 2, offset = 24)

        assertEquals(
            listOf(
                NetworkPokemon(id = 25, name = "pikachu", imageUrl = "https://example.com/25.png"),
                NetworkPokemon(id = 26, name = "raichu", imageUrl = "https://example.com/26.png"),
            ),
            pokemon,
        )
    }

    @Test
    fun getPokemon_responseWithErrors_throwsWithMessages() = runTest {
        server.enqueueJson(
            """
            {
              "data": null,
              "errors": [
                { "message": "Unknown argument \"limt\"" },
                { "message": "Rate limited" }
              ]
            }
            """.trimIndent(),
        )

        val error = assertFailsWith<GraphQlException> {
            dataSource.getPokemon(limit = 2, offset = 0)
        }
        assertEquals("Unknown argument \"limt\"; Rate limited", error.message)
    }

    @Test
    fun getPokemon_responseWithoutData_throws() = runTest {
        server.enqueueJson("""{ "data": null }""")

        assertFailsWith<GraphQlException> {
            dataSource.getPokemon(limit = 2, offset = 0)
        }
    }
}

private val POKEMONS_JSON = """
    {
      "data": {
        "pokemons": {
          "count": 1351,
          "results": [
            { "id": 25, "name": "pikachu", "image": "https://example.com/25.png" },
            { "id": 26, "name": "raichu", "image": "https://example.com/26.png" }
          ]
        }
      }
    }
""".trimIndent()
