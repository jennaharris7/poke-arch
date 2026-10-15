package com.fatbird.pokearch.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.fatbird.pokearch.core.database.model.PokemonEntity
import com.fatbird.pokearch.core.model.ApiSource
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonDao {

    @Query("SELECT * FROM pokemon WHERE source = :source ORDER BY id")
    fun getPokemon(source: ApiSource): Flow<List<PokemonEntity>>

    @Upsert
    suspend fun upsertPokemon(entities: List<PokemonEntity>)
}
