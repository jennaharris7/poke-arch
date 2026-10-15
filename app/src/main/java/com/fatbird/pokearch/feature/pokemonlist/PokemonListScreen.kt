package com.fatbird.pokearch.feature.pokemonlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fatbird.pokearch.R
import com.fatbird.pokearch.core.designsystem.component.PokeArchAsyncImage
import com.fatbird.pokearch.core.designsystem.theme.PokeArchTheme
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.model.Pokemon
import com.fatbird.pokearch.core.ui.titleRes

@Composable
fun PokemonListRoute(
    apiSource: ApiSource,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel =
        hiltViewModel<PokemonListViewModel, PokemonListViewModel.Factory>(
            creationCallback = { factory -> factory.create(apiSource) },
        ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PokemonListScreen(
        apiSource = apiSource,
        uiState = uiState,
        onRefresh = viewModel::refresh,
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PokemonListScreen(
    apiSource: ApiSource,
    uiState: PokemonListUiState,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val refreshFailedMessage = stringResource(R.string.feature_pokemonlist_refresh_failed)
    val refreshFailed = (uiState as? PokemonListUiState.Success)?.refreshFailed == true
    LaunchedEffect(refreshFailed) {
        if (refreshFailed) snackbarHostState.showSnackbar(refreshFailedMessage)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            R.string.feature_pokemonlist_title,
                            stringResource(apiSource.titleRes),
                        ),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.core_ui_back),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (uiState) {
            PokemonListUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            PokemonListUiState.Error -> ErrorContent(
                onRetry = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            is PokemonListUiState.Success -> PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
            ) {
                // Let the grid scroll behind the navigation bar while keeping its last row reachable.
                val layoutDirection = LocalLayoutDirection.current
                PokemonGrid(
                    pokemon = uiState.pokemon,
                    contentPadding = PaddingValues(
                        start = innerPadding.calculateStartPadding(layoutDirection) + 16.dp,
                        top = 16.dp,
                        end = innerPadding.calculateEndPadding(layoutDirection) + 16.dp,
                        bottom = innerPadding.calculateBottomPadding() + 16.dp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun PokemonGrid(
    pokemon: List<Pokemon>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(pokemon, key = { it.id }) { pokemon ->
            PokemonCard(pokemon = pokemon)
        }
    }
}

@Composable
private fun PokemonCard(
    pokemon: Pokemon,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PokeArchAsyncImage(
                imageUrl = pokemon.imageUrl,
                // The name is announced by the text below.
                contentDescription = null,
                // Sprites are small pixel art; skip smoothing so they stay crisp when scaled up.
                filterQuality = FilterQuality.None,
                modifier = Modifier.size(96.dp),
            )
            Text(
                text = stringResource(R.string.feature_pokemonlist_number, pokemon.id),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = pokemon.name.replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.feature_pokemonlist_error),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.feature_pokemonlist_retry))
        }
    }
}

private val previewPokemon = listOf(
    Pokemon(id = 1, name = "bulbasaur", imageUrl = ""),
    Pokemon(id = 4, name = "charmander", imageUrl = ""),
    Pokemon(id = 7, name = "squirtle", imageUrl = ""),
    Pokemon(id = 25, name = "pikachu", imageUrl = ""),
)

@PreviewLightDark
@Composable
private fun PokemonListScreenSuccessPreview() {
    PokeArchTheme {
        PokemonListScreen(
            apiSource = ApiSource.GRAPHQL,
            uiState = PokemonListUiState.Success(previewPokemon),
            onRefresh = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
private fun PokemonListScreenLoadingPreview() {
    PokeArchTheme {
        PokemonListScreen(
            apiSource = ApiSource.REST,
            uiState = PokemonListUiState.Loading,
            onRefresh = {},
            onBackClick = {},
        )
    }
}

@Preview
@Composable
private fun PokemonListScreenErrorPreview() {
    PokeArchTheme {
        PokemonListScreen(
            apiSource = ApiSource.REST,
            uiState = PokemonListUiState.Error,
            onRefresh = {},
            onBackClick = {},
        )
    }
}
