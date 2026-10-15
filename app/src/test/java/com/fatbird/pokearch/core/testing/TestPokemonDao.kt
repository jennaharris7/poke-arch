package com.fatbird.pokearch.core.testing

import androidx.paging.PagingSource
import androidx.paging.testing.asPagingSourceFactory
import com.fatbird.pokearch.core.database.dao.PokemonDao
import com.fatbird.pokearch.core.database.model.PokemonEntity
import com.fatbird.pokearch.core.database.model.PokemonRemoteKeysEntity
import com.fatbird.pokearch.core.model.ApiSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory [PokemonDao] that mirrors the real DAO's keying and ordering. Paging sources it hands
 * out are invalidated on every write, like Room's.
 */
class TestPokemonDao : PokemonDao {

    private val entities = MutableStateFlow(emptyList<PokemonEntity>())
    private val remoteKeys = mutableMapOf<ApiSource, PokemonRemoteKeysEntity>()
    private val pagingSources = mutableListOf<PagingSource<Int, PokemonEntity>>()

    override fun getPokemon(source: ApiSource): Flow<List<PokemonEntity>> =
        entities.map { all -> all.filter { it.source == source }.sortedBy(PokemonEntity::id) }

    override fun pagingSource(source: ApiSource): PagingSource<Int, PokemonEntity> =
        entities.value
            .filter { it.source == source }
            .sortedBy(PokemonEntity::id)
            .asPagingSourceFactory()
            .invoke()
            .also(pagingSources::add)

    override suspend fun upsertPokemon(entities: List<PokemonEntity>) {
        this.entities.update { existing ->
            // Later entries win for the same (source, id), matching @Upsert.
            (existing + entities).associateBy { it.source to it.id }.values.toList()
        }
        invalidatePagingSources()
    }

    override suspend fun deletePokemon(source: ApiSource) {
        entities.update { existing -> existing.filterNot { it.source == source } }
        invalidatePagingSources()
    }

    override suspend fun getRemoteKeys(source: ApiSource): PokemonRemoteKeysEntity? =
        remoteKeys[source]

    override suspend fun upsertRemoteKeys(remoteKeys: PokemonRemoteKeysEntity) {
        this.remoteKeys[remoteKeys.source] = remoteKeys
    }

    private fun invalidatePagingSources() {
        pagingSources.forEach(PagingSource<Int, PokemonEntity>::invalidate)
        pagingSources.clear()
    }
}
