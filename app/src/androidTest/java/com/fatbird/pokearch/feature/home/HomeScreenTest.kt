package com.fatbird.pokearch.feature.home

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.fatbird.pokearch.R
import com.fatbird.pokearch.core.model.ApiSource
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun string(id: Int) = composeTestRule.activity.getString(id)

    @Test
    fun showsEveryApiSource() {
        composeTestRule.setContent {
            HomeScreen(onApiSourceClick = {})
        }

        composeTestRule.onNodeWithText(string(R.string.core_ui_api_source_rest)).assertIsDisplayed()
        composeTestRule.onNodeWithText(string(R.string.core_ui_api_source_graphql)).assertIsDisplayed()
    }

    @Test
    fun clickingApiSource_reportsIt() {
        var clicked: ApiSource? = null
        composeTestRule.setContent {
            HomeScreen(onApiSourceClick = { clicked = it })
        }

        composeTestRule.onNodeWithText(string(R.string.core_ui_api_source_graphql)).performClick()

        assertEquals(ApiSource.GRAPHQL, clicked)
    }
}
