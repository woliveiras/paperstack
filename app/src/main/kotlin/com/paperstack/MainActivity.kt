package com.paperstack

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.paperstack.ui.detail.DetailScreen
import com.paperstack.ui.feed.FeedScreen
import com.paperstack.ui.onboarding.CategoriesStep
import com.paperstack.ui.onboarding.OnboardingScreen
import com.paperstack.ui.onboarding.OnboardingViewModel
import com.paperstack.ui.saved.SavedScreen
import com.paperstack.ui.theme.PaperstackTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val bottomNavRoutes = listOf("feed", "saved")

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
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                startDestination?.let { destination ->
                    val showBottomNav = currentRoute in bottomNavRoutes

                    Scaffold(
                        bottomBar = {
                            if (showBottomNav) {
                                NavigationBar {
                                    NavigationBarItem(
                                        selected = currentRoute == "feed",
                                        onClick = {
                                            navController.navigate("feed") {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentRoute == "feed") Icons.Filled.Home else Icons.Outlined.Home,
                                                contentDescription = "Feed",
                                            )
                                        },
                                        label = { Text("Feed") },
                                    )
                                    NavigationBarItem(
                                        selected = currentRoute == "saved",
                                        onClick = {
                                            navController.navigate("saved") {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (currentRoute == "saved") Icons.Filled.Bookmarks else Icons.Outlined.BookmarkBorder,
                                                contentDescription = "Saved",
                                            )
                                        },
                                        label = { Text("Saved") },
                                    )
                                }
                            }
                        },
                    ) { _ ->
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
                                        onPaperClick = { paper ->
                                            val paperJson = Uri.encode(Json.encodeToString(paper))
                                            navController.navigate("detail/$paperJson")
                                        },
                                        onAddCategories = {
                                            navController.navigate("add-categories")
                                        },
                                        onCategorySwitch = { code ->
                                            mainViewModel.setActiveCategory(code)
                                        },
                                    )
                                }
                            }
                            composable("saved") {
                                SavedScreen(
                                    onPaperClick = { paper ->
                                        val paperJson = Uri.encode(Json.encodeToString(paper))
                                        navController.navigate("detail/$paperJson")
                                    },
                                )
                            }
                            composable(
                                route = "detail/{paperJson}",
                                arguments = listOf(navArgument("paperJson") { type = NavType.StringType }),
                            ) {
                                DetailScreen(onBack = { navController.popBackStack() })
                            }
                            composable("add-categories") {
                                val addViewModel: OnboardingViewModel = hiltViewModel()
                                val currentCategories = settings?.selectedCategories.orEmpty()
                                LaunchedEffect(Unit) {
                                    addViewModel.initForAddCategories(currentCategories)
                                }
                                val state by addViewModel.state.collectAsState()
                                LaunchedEffect(state.isDone) {
                                    if (state.isDone) navController.popBackStack()
                                }
                                CategoriesStep(
                                    state = state,
                                    onToggleCategory = addViewModel::toggleCategory,
                                    onConfirm = addViewModel::saveAddedCategories,
                                    confirmLabel = "Save categories",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


