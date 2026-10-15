package com.fatbird.pokearch.feature.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.fatbird.pokearch.R
import com.fatbird.pokearch.core.designsystem.theme.PokeArchTheme
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.ui.titleRes

/**
 * Lets the user pick which PokeAPI flavor to load Pokémon from.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onApiSourceClick: (ApiSource) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(text = stringResource(R.string.app_name)) })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.feature_home_prompt),
                style = MaterialTheme.typography.titleMedium,
            )
            ApiSource.entries.forEach { apiSource ->
                ApiSourceCard(
                    apiSource = apiSource,
                    onClick = { onApiSourceClick(apiSource) },
                )
            }
        }
    }
}

@Composable
private fun ApiSourceCard(
    apiSource: ApiSource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(apiSource.titleRes),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = stringResource(apiSource.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@get:StringRes
private val ApiSource.descriptionRes: Int
    get() = when (this) {
        ApiSource.REST -> R.string.feature_home_rest_description
        ApiSource.GRAPHQL -> R.string.feature_home_graphql_description
    }

@PreviewLightDark
@Composable
private fun HomeScreenPreview() {
    PokeArchTheme {
        HomeScreen(onApiSourceClick = {})
    }
}
