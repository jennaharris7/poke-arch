package com.fatbird.pokearch.core.database.dao

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.testing.TestPager
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.fatbird.pokearch.core.database.PokeArchDatabase
import com.fatbird.pokearch.core.database.model.PokemonEntity
import com.fatbird.pokearch.core.database.model.PokemonRemoteKeysEntity
import com.fatbird.pokearch.core.model.ApiSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

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

    @Test
    fun pagingSource_loadsOnlyRequestedSourceOrderedById() = runTest {
        pokemonDao.upsertPokemon(
            listOf(
                testEntity(ApiSource.REST, id = 25),
                testEntity(ApiSource.GRAPHQL, id = 4),
                testEntity(ApiSource.REST, id = 1),
                testEntity(ApiSource.REST, id = 7),
            ),
        )
        val pager = TestPager(
            config = PagingConfig(pageSize = 2, initialLoadSize = 2, enablePlaceholders = false),
            pagingSource = pokemonDao.pagingSource(ApiSource.REST),
        )

        val firstPage = assertIs<PagingSource.LoadResult.Page<Int, PokemonEntity>>(pager.refresh())
        val secondPage = assertIs<PagingSource.LoadResult.Page<Int, PokemonEntity>>(pager.append())

        assertEquals(
            listOf(1, 7, 25),
            (firstPage.data + secondPage.data).map(PokemonEntity::id),
        )
    }

    @Test
    fun getRemoteKeys_nothingLoaded_isNull() = runTest {
        assertNull(pokemonDao.getRemoteKeys(ApiSource.REST))
    }

    @Test
    fun replacePokemon_clearsOnlyThatSourceAndStoresNextOffset() = runTest {
        pokemonDao.upsertPokemon(
            listOf(
                testEntity(ApiSource.REST, id = 1),
                testEntity(ApiSource.REST, id = 2),
                testEntity(ApiSource.GRAPHQL, id = 1),
            ),
        )

        pokemonDao.replacePokemon(
            source = ApiSource.REST,
            entities = listOf(testEntity(ApiSource.REST, id = 3)),
            nextOffset = 1,
        )

        assertEquals(listOf(3), pokemonDao.getPokemon(ApiSource.REST).first().map(PokemonEntity::id))
        assertEquals(1, pokemonDao.getPokemon(ApiSource.GRAPHQL).first().size)
        assertEquals(
            PokemonRemoteKeysEntity(source = ApiSource.REST, nextOffset = 1),
            pokemonDao.getRemoteKeys(ApiSource.REST),
        )
        assertNull(pokemonDao.getRemoteKeys(ApiSource.GRAPHQL))
    }

    @Test
    fun appendPokemon_keepsCachedRowsAndAdvancesNextOffset() = runTest {
        pokemonDao.replacePokemon(
            source = ApiSource.REST,
            entities = listOf(testEntity(ApiSource.REST, id = 1)),
            nextOffset = 1,
        )

        pokemonDao.appendPokemon(
            source = ApiSource.REST,
            entities = listOf(testEntity(ApiSource.REST, id = 2)),
            nextOffset = null,
        )

        assertEquals(
            listOf(1, 2),
            pokemonDao.getPokemon(ApiSource.REST).first().map(PokemonEntity::id),
        )
        assertEquals(
            PokemonRemoteKeysEntity(source = ApiSource.REST, nextOffset = null),
            pokemonDao.getRemoteKeys(ApiSource.REST),
        )
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
