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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.fatbird.pokearch.R
import com.fatbird.pokearch.core.designsystem.component.PokeArchAsyncImage
import com.fatbird.pokearch.core.designsystem.theme.PokeArchTheme
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.model.Pokemon
import com.fatbird.pokearch.core.ui.titleRes
import kotlinx.coroutines.flow.flowOf
import java.io.IOException

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
    PokemonListScreen(
        apiSource = apiSource,
        pokemon = viewModel.pokemon.collectAsLazyPagingItems(),
        onBackClick = onBackClick,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PokemonListScreen(
    apiSource: ApiSource,
    pokemon: LazyPagingItems<Pokemon>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshState = pokemon.loadState.refresh
    val hasPokemon = pokemon.itemCount > 0

    val snackbarHostState = remember { SnackbarHostState() }
    val refreshFailedMessage = stringResource(R.string.feature_pokemonlist_refresh_failed)
    // Keyed on the error itself so each failed refresh is announced once.
    val refreshError = (refreshState as? LoadState.Error)?.takeIf { hasPokemon }
    LaunchedEffect(refreshError) {
        if (refreshError != null) snackbarHostState.showSnackbar(refreshFailedMessage)
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
        when {
            hasPokemon -> PullToRefreshBox(
                isRefreshing = refreshState is LoadState.Loading,
                onRefresh = pokemon::refresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding()),
            ) {
                // Let the grid scroll behind the navigation bar while keeping its last row reachable.
                val layoutDirection = LocalLayoutDirection.current
                PokemonGrid(
                    pokemon = pokemon,
                    contentPadding = PaddingValues(
                        start = innerPadding.calculateStartPadding(layoutDirection) + 16.dp,
                        top = 16.dp,
                        end = innerPadding.calculateEndPadding(layoutDirection) + 16.dp,
                        bottom = innerPadding.calculateBottomPadding() + 16.dp,
                    ),
                )
            }

            refreshState is LoadState.Error -> ErrorContent(
                onRetry = pokemon::retry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )

            else -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun PokemonGrid(
    pokemon: LazyPagingItems<Pokemon>,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val appendState = pokemon.loadState.append
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            count = pokemon.itemCount,
            key = pokemon.itemKey { it.id },
            contentType = pokemon.itemContentType { "pokemon" },
        ) { index ->
            // Null while a placeholder's page is still loading from the cache.
            PokemonCard(pokemon = pokemon[index])
        }
        if (appendState !is LoadState.NotLoading) {
            item(span = { GridItemSpan(maxLineSpan) }, contentType = "appendState") {
                AppendStateFooter(appendState = appendState, onRetry = pokemon::retry)
            }
        }
    }
}

@Composable
private fun AppendStateFooter(
    appendState: LoadState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (appendState is LoadState.Error) {
            Text(
                text = stringResource(R.string.feature_pokemonlist_append_error),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.feature_pokemonlist_retry))
            }
        } else {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun PokemonCard(
    pokemon: Pokemon?,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Placeholders keep the same layout with blank content so the grid doesn't shift once
            // their page loads.
            if (pokemon != null) {
                PokeArchAsyncImage(
                    imageUrl = pokemon.imageUrl,
                    // The name is announced by the text below.
                    contentDescription = null,
                    // Sprites are small pixel art; skip smoothing so they stay crisp when scaled up.
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.size(96.dp),
                )
            } else {
                Box(modifier = Modifier.size(96.dp))
            }
            Text(
                text = pokemon?.let {
                    stringResource(R.string.feature_pokemonlist_number, it.id)
                }.orEmpty(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = pokemon?.name?.replaceFirstChar { it.titlecase() }.orEmpty(),
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

@Composable
private fun previewPagingItems(
    pokemon: List<Pokemon> = emptyList(),
    refresh: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
): LazyPagingItems<Pokemon> {
    val notLoading = LoadState.NotLoading(endOfPaginationReached = false)
    val loadStates = LoadStates(refresh = refresh, prepend = notLoading, append = notLoading)
    return remember(pokemon, refresh) {
        flowOf(PagingData.from(pokemon, sourceLoadStates = loadStates))
    }.collectAsLazyPagingItems()
}

@PreviewLightDark
@Composable
private fun PokemonListScreenSuccessPreview() {
    PokeArchTheme {
        PokemonListScreen(
            apiSource = ApiSource.GRAPHQL,
            pokemon = previewPagingItems(pokemon = previewPokemon),
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
            pokemon = previewPagingItems(refresh = LoadState.Loading),
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
            pokemon = previewPagingItems(refresh = LoadState.Error(IOException("offline"))),
            onBackClick = {},
        )
    }
}
