package com.fatbird.pokearch.core.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fatbird.pokearch.core.database.PokeArchDatabase
import com.fatbird.pokearch.core.database.model.PokemonEntity
import com.fatbird.pokearch.core.model.ApiSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class PokemonDaoTest {

    private lateinit var database: PokeArchDatabase
    private lateinit var pokemonDao: PokemonDao

    @Before
    fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            PokeArchDatabase::class.java,
        ).build()
        pokemonDao = database.pokemonDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun getPokemon_returnsOnlyRequestedSourceOrderedById() = runTest {
        pokemonDao.upsertPokemon(
            listOf(
                testEntity(ApiSource.REST, id = 25),
                testEntity(ApiSource.GRAPHQL, id = 4),
                testEntity(ApiSource.REST, id = 1),
                testEntity(ApiSource.REST, id = 7),
            ),
        )

        val restPokemon = pokemonDao.getPokemon(ApiSource.REST).first()

        assertEquals(listOf(1, 7, 25), restPokemon.map(PokemonEntity::id))
        assertEquals(setOf(ApiSource.REST), restPokemon.map(PokemonEntity::source).toSet())
    }

    @Test
    fun upsertPokemon_sameSourceAndId_replacesRow() = runTest {
        pokemonDao.upsertPokemon(listOf(testEntity(ApiSource.REST, id = 25, name = "pika")))
        pokemonDao.upsertPokemon(listOf(testEntity(ApiSource.REST, id = 25, name = "pikachu")))

        assertEquals(
            listOf(testEntity(ApiSource.REST, id = 25, name = "pikachu")),
            pokemonDao.getPokemon(ApiSource.REST).first(),
        )
    }

    @Test
    fun upsertPokemon_sameIdDifferentSource_keepsBothRows() = runTest {
        pokemonDao.upsertPokemon(
            listOf(
                testEntity(ApiSource.REST, id = 25),
                testEntity(ApiSource.GRAPHQL, id = 25),
            ),
        )

        assertEquals(1, pokemonDao.getPokemon(ApiSource.REST).first().size)
        assertEquals(1, pokemonDao.getPokemon(ApiSource.GRAPHQL).first().size)
    }
}

private fun testEntity(
    source: ApiSource,
    id: Int,
    name: String = "pokemon-$id",
) = PokemonEntity(
    source = source,
    id = id,
    name = name,
    imageUrl = "https://example.com/$id.png",
)
