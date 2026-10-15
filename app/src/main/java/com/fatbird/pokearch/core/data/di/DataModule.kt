package com.fatbird.pokearch.core.data.di

import com.fatbird.pokearch.core.data.repository.OfflineFirstPokemonRepository
import com.fatbird.pokearch.core.data.repository.PokemonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface DataModule {

    @Binds
    fun bindsPokemonRepository(
        pokemonRepository: OfflineFirstPokemonRepository,
    ): PokemonRepository
}
