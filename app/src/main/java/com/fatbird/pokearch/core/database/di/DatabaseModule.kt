package com.fatbird.pokearch.core.database.di

import android.content.Context
import androidx.room.Room
import com.fatbird.pokearch.core.database.PokeArchDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {

    @Provides
    @Singleton
    fun providesPokeArchDatabase(
        @ApplicationContext context: Context,
    ): PokeArchDatabase = Room.databaseBuilder(
        context,
        PokeArchDatabase::class.java,
        "poke-arch-database",
    ).build()
}
