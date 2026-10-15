package com.fatbird.pokearch.feature.pokemonlist

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.fatbird.pokearch.R
import com.fatbird.pokearch.core.model.ApiSource
import com.fatbird.pokearch.core.model.Pokemon
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class PokemonListScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val samplePokemon = listOf(
        Pokemon(id = 1, name = "bulbasaur", imageUrl = ""),
        Pokemon(id = 25, name = "pikachu", imageUrl = ""),
    )

    private fun string(id: Int, vararg args: Any) = composeTestRule.activity.getString(id, *args)

    private fun setScreen(
        uiState: PokemonListUiState,
        apiSource: ApiSource = ApiSource.GRAPHQL,
        onRefresh: () -> Unit = {},
        onBackClick: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            PokemonListScreen(
                apiSource = apiSource,
                uiState = uiState,
                onRefresh = onRefresh,
                onBackClick = onBackClick,
            )
        }
    }

    @Test
    fun title_namesApiSource() {
        setScreen(PokemonListUiState.Loading, apiSource = ApiSource.REST)

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
        setScreen(PokemonListUiState.Loading)

        composeTestRule
            .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
            .assertIsDisplayed()
    }

    @Test
    fun success_showsNumberAndCapitalizedName() {
        setScreen(PokemonListUiState.Success(samplePokemon))

        composeTestRule.onNodeWithText("Pikachu").assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.feature_pokemonlist_number, 25)).assertIsDisplayed()
    }

    @Test
    fun error_retryRequestsRefresh() {
        var refreshCount = 0
        setScreen(PokemonListUiState.Error, onRefresh = { refreshCount++ })

        composeTestRule.onNodeWithText(string(R.string.feature_pokemonlist_error)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.feature_pokemonlist_retry)).performClick()

        assertEquals(1, refreshCount)
    }

    @Test
    fun refreshFailedWithCache_showsSnackbar() {
        setScreen(PokemonListUiState.Success(samplePokemon, refreshFailed = true))

        composeTestRule
            .onNodeWithText(string(R.string.feature_pokemonlist_refresh_failed))
            .assertIsDisplayed()
    }

    @Test
    fun backButton_requestsBack() {
        var backClicked = false
        setScreen(PokemonListUiState.Loading, onBackClick = { backClicked = true })

        composeTestRule
            .onNodeWithContentDescription(string(R.string.core_ui_back))
            .performClick()

        assertEquals(true, backClicked)
    }
}
