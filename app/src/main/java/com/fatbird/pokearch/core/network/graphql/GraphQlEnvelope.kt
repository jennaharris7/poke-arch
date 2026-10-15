package com.fatbird.pokearch.core.network.graphql

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * Standard GraphQL-over-HTTP request body.
 */
@Serializable
internal data class GraphQlRequest(
    val query: String,
    val variables: JsonObject = JsonObject(emptyMap()),
)

/**
 * Standard GraphQL-over-HTTP response body. A response may carry partial [data] alongside [errors].
 */
@Serializable
internal data class GraphQlResponse<T>(
    val data: T? = null,
    val errors: List<GraphQlError>? = null,
)

@Serializable
internal data class GraphQlError(
    val message: String,
)
