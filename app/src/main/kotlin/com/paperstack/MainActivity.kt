package com.paperstack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.paperstack.ui.feed.FeedScreen
import com.paperstack.ui.onboarding.OnboardingScreen
import com.paperstack.ui.theme.PaperstackTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaperstackTheme {
                val navController = rememberNavController()
                val mainViewModel: MainViewModel = hiltViewModel()
                val startDestination by mainViewModel.startDestination.collectAsState()
                val settings by mainViewModel.settings.collectAsState()

                startDestination?.let { destination ->
                    NavHost(navController = navController, startDestination = destination) {
                        composable("onboarding") {
                            OnboardingScreen(
                                onComplete = {
                                    navController.navigate("feed") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                },
                            )
                        }
                        composable("feed") {
                            settings?.let { s ->
                                FeedScreen(
                                    settings = s,
                                    onPaperClick = { /* spec 0003 — navigate to detail */ },
                                    onAddCategories = {
                                        navController.navigate("onboarding")
                                    },
                                    onCategorySwitch = { code ->
                                        mainViewModel.setActiveCategory(code)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

