package com.fatbird.pokearch.core.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.fatbird.pokearch.core.database.model.PokemonEntity
import com.fatbird.pokearch.core.database.model.PokemonRemoteKeysEntity
import com.fatbird.pokearch.core.model.ApiSource
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    @Query("SELECT * FROM pokemon WHERE source = :source ORDER BY id")
    fun getPokemon(source: ApiSource): Flow<List<PokemonEntity>>

    @Query("SELECT * FROM pokemon WHERE source = :source ORDER BY id")
    fun pagingSource(source: ApiSource): PagingSource<Int, PokemonEntity>

    @Upsert
    suspend fun upsertPokemon(entities: List<PokemonEntity>)

    @Query("DELETE FROM pokemon WHERE source = :source")
    suspend fun deletePokemon(source: ApiSource)

    @Query("SELECT * FROM pokemon_remote_keys WHERE source = :source")
    suspend fun getRemoteKeys(source: ApiSource): PokemonRemoteKeysEntity?

    @Upsert
    suspend fun upsertRemoteKeys(remoteKeys: PokemonRemoteKeysEntity)

    /**
     * Replaces everything cached for [source] with a freshly loaded first page, so rows dropped
     * upstream don't linger.
     */
    @Transaction
    suspend fun replacePokemon(
        source: ApiSource,
        entities: List<PokemonEntity>,
        nextOffset: Int?,
    ) {
        deletePokemon(source)
        appendPokemon(source, entities, nextOffset)
    }

    /**
     * Caches another page for [source] together with where the following page starts.
     */
    @Transaction
    suspend fun appendPokemon(
        source: ApiSource,
        entities: List<PokemonEntity>,
        nextOffset: Int?,
    ) {
        upsertPokemon(entities)
        upsertRemoteKeys(PokemonRemoteKeysEntity(source = source, nextOffset = nextOffset))
    }
}
