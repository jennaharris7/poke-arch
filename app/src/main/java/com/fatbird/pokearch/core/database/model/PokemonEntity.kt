package com.fatbird.pokearch.core.database.model

import androidx.room.Entity
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.model.Pokemon

/**
 * A cached Pokémon. Rows are keyed by [source] as well as [id] so each API keeps its own cache,
 * making it visible which API produced the data on screen.
 */
@Entity(
    tableName = "pokemon",
    primaryKeys = ["source", "id"],
)
data class PokemonEntity(
    val source: ApiSource,
    val id: Int,
    val name: String,
    val imageUrl: String,
)

fun PokemonEntity.asExternalModel() = Pokemon(
    id = id,
    name = name,
    imageUrl = imageUrl,
)
