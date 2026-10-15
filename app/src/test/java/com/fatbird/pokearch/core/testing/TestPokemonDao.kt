package com.fatbird.pokearch.core.testing

import com.fatbird.pokearch.core.database.dao.PokemonDao
import com.fatbird.pokearch.core.database.model.PokemonEntity
import com.fatbird.pokearch.core.model.ApiSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * In-memory [PokemonDao] that mirrors the real DAO's keying and ordering.
 */
class TestPokemonDao : PokemonDao {

    private val entities = MutableStateFlow(emptyList<PokemonEntity>())

    override fun getPokemon(source: ApiSource): Flow<List<PokemonEntity>> =
        entities.map { all -> all.filter { it.source == source }.sortedBy(PokemonEntity::id) }

    override suspend fun upsertPokemon(entities: List<PokemonEntity>) {
        this.entities.update { existing ->
            // Later entries win for the same (source, id), matching @Upsert.
            (existing + entities).associateBy { it.source to it.id }.values.toList()
        }
    }
}
