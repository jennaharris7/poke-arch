package com.fatbird.pokearch.feature.pokemonlist

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.collectAsLazyPagingItems
import com.fatbird.pokearch.R
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.model.Pokemon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals

class PokemonListScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val samplePokemon = listOf(
        Pokemon(id = 1, name = "bulbasaur", imageUrl = ""),
        Pokemon(id = 25, name = "pikachu", imageUrl = ""),
    )

    private fun string(id: Int, vararg args: Any) = composeTestRule.activity.getString(id, *args)

    private val notLoading = LoadState.NotLoading(endOfPaginationReached = false)

    /** Paging data frozen in the given load states. */
    private fun pagingData(
        pokemon: List<Pokemon> = emptyList(),
        refresh: LoadState = notLoading,
        append: LoadState = notLoading,
    ): Flow<PagingData<Pokemon>> = flowOf(
        PagingData.from(
            pokemon,
            sourceLoadStates = LoadStates(refresh = refresh, prepend = notLoading, append = append),
        ),
    )

    private fun setScreen(
        pokemon: Flow<PagingData<Pokemon>>,
        apiSource: ApiSource = ApiSource.GRAPHQL,
        onBackClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            PokemonListScreen(
                apiSource = apiSource,
                pokemon = pokemon.collectAsLazyPagingItems(),
                onBackClick = onBackClick,
            )
        }
    }

    @Test
    fun title_namesApiSource() {
        setScreen(pagingData(refresh = LoadState.Loading), apiSource = ApiSource.REST)

        composeTestRule
            .onNodeWithText(
                string(
                    R.string.feature_pokemonlist_title,
                    string(R.string.core_ui_api_source_rest),
                ),
            )
            .assertIsDisplayed()
    }

    @Test
    fun loading_showsProgressIndicator() {
        setScreen(pagingData(refresh = LoadState.Loading))

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun success_showsNumberAndCapitalizedName() {
        setScreen(pagingData(samplePokemon))

        composeTestRule.onNodeWithText("Pikachu").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.feature_pokemonlist_number, 25)).assertIsDisplayed()
    }

    @Test
    fun error_retryLoadsPokemon() {
        val pager = Pager(PagingConfig(pageSize = 20)) { FailOnceThenSucceedPagingSource(samplePokemon) }
        setScreen(pager.flow)

        composeTestRule.onNodeWithText(string(R.string.feature_pokemonlist_error)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.feature_pokemonlist_retry)).performClick()

        composeTestRule.onNodeWithText("Pikachu").assertIsDisplayed()
    }

    @Test
    fun refreshFailedWithCache_showsSnackbar() {
        setScreen(pagingData(samplePokemon, refresh = LoadState.Error(IOException("offline"))))

        composeTestRule
            .onNodeWithText(string(R.string.feature_pokemonlist_refresh_failed))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Pikachu").assertIsDisplayed()
    }

    @Test
    fun appendFailed_showsRetryFooter() {
        setScreen(pagingData(samplePokemon, append = LoadState.Error(IOException("offline"))))

        composeTestRule
            .onNodeWithText(string(R.string.feature_pokemonlist_append_error))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.feature_pokemonlist_retry)).assertIsDisplayed()
    }

    @Test
    fun backButton_requestsBack() {
        var backClicked = false
        setScreen(pagingData(refresh = LoadState.Loading), onBackClick = { backClicked = true })

        composeTestRule
            .onNodeWithContentDescription(string(R.string.core_ui_back))
            .performClick()

        assertEquals(true, backClicked)
    }
}

/** Fails its first load, then serves [pokemon] as a single page. */
private class FailOnceThenSucceedPagingSource(
    private val pokemon: List<Pokemon>,
) : PagingSource<Int, Pokemon>() {

    private var failed = false

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Pokemon> {
        if (!failed) {
            failed = true
            return LoadResult.Error(IOException("offline"))
        }
        return LoadResult.Page(data = pokemon, prevKey = null, nextKey = null)
    }

    override fun getRefreshKey(state: PagingState<Int, Pokemon>): Int? = null
}
