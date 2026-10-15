package com.fatbird.pokearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fatbird.pokearch.core.designsystem.theme.PokeArchTheme
import com.fatbird.pokearch.ui.PokeArchApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokeArchTheme {
                PokeArchApp()
            }
        }
    }
}
