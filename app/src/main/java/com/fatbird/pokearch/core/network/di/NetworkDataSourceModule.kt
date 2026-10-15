package com.fatbird.pokearch.core.network.di

import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.network.PokemonNetworkDataSource
import com.fatbird.pokearch.core.network.graphql.GraphQlPokemonNetworkDataSource
import com.fatbird.pokearch.core.network.rest.RestPokemonNetworkDataSource
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

/**
 * Keys [PokemonNetworkDataSource] implementations in a `Map<ApiSource, PokemonNetworkDataSource>`.
 */
@MapKey
annotation class ApiSourceKey(val value: ApiSource)

@Module
@InstallIn(SingletonComponent::class)
internal interface NetworkDataSourceModule {

    @Binds
    @IntoMap
    @ApiSourceKey(ApiSource.REST)
    fun bindsRestPokemonNetworkDataSource(
        dataSource: RestPokemonNetworkDataSource,
    ): PokemonNetworkDataSource

    @Binds
    @IntoMap
    @ApiSourceKey(ApiSource.GRAPHQL)
    fun bindsGraphQlPokemonNetworkDataSource(
        dataSource: GraphQlPokemonNetworkDataSource,
    ): PokemonNetworkDataSource
}
