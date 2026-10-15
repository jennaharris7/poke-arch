package com.fatbird.pokearch.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fatbird.pokearch.core.model.ApiSource

/**
 * Paging position for the Pokémon cached from [source]. Pages are only ever appended from offset 0,
 * so a single row per source is enough to know where the next page starts.
 *
 * @param nextOffset offset of the next page to fetch, or null once the end of the list is reached.
 */
@Entity(tableName = "pokemon_remote_keys")
data class PokemonRemoteKeysEntity(
    @PrimaryKey val source: ApiSource,
    val nextOffset: Int?,
)
