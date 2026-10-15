package com.fatbird.pokearch.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.fatbird.pokearch.core.database.dao.PokemonDao
import com.fatbird.pokearch.core.database.model.PokemonEntity
import com.fatbird.pokearch.core.database.model.PokemonRemoteKeysEntity

@Database(
    entities = [
        PokemonEntity::class,
        PokemonRemoteKeysEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
internal abstract class PokeArchDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
}
