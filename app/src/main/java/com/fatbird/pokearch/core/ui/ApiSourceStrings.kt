package com.fatbird.pokearch.core.ui

import androidx.annotation.StringRes
import com.fatbird.pokearch.R
import com.fatbird.pokearch.core.model.ApiSource

/**
 * User-facing name of an [ApiSource].
 */
@get:StringRes
val ApiSource.titleRes: Int
    get() = when (this) {
        ApiSource.REST -> R.string.core_ui_api_source_rest
        ApiSource.GRAPHQL -> R.string.core_ui_api_source_graphql
    }
