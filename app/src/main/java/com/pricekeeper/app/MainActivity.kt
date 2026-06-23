package com.pricekeeper.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.pricekeeper.app.core.ui.theme.PriceKeeperTheme
import com.pricekeeper.app.core.ui.theme.ThemePreferences
import com.pricekeeper.app.feature.main.MainScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkMode by ThemePreferences.observeDarkMode(this).collectAsState(initial = false)

            PriceKeeperTheme(darkThemeOverride = darkMode) {
                MainScreen()
            }
        }
    }
}
