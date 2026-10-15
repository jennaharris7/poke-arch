package com.fatbird.pokearch.core.database.di

import com.fatbird.pokearch.core.database.PokeArchDatabase
import com.fatbird.pokearch.core.database.dao.PokemonDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {

    @Provides
    fun providesPokemonDao(
        database: PokeArchDatabase,
    ): PokemonDao = database.pokemonDao()
}
