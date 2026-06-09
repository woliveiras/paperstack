package com.paperstack

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.paperstack.data.repository.PaperNavigationCache
import com.paperstack.ui.detail.DetailScreen
import com.paperstack.ui.feed.FeedScreen
import com.paperstack.ui.feed.FeedViewModel
import com.paperstack.ui.feed.FilterScreen
import com.paperstack.ui.onboarding.CategoriesStep
import com.paperstack.ui.onboarding.OnboardingScreen
import com.paperstack.ui.onboarding.OnboardingViewModel
import com.paperstack.ui.saved.SavedScreen
import com.paperstack.ui.theme.PaperStackTheme
import com.paperstack.ui.theme.Spacing
import dagger.hilt.android.AndroidEntryPoint

private val bottomNavRoutes = listOf("feed_home", "saved")

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @javax.inject.Inject
    lateinit var paperNavigationCache: PaperNavigationCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaperStackTheme {
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
                                PaperStackBottomNav(
                                    currentRoute = currentRoute,
                                    onTabSelected = { route ->
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                )
                            }
                        },
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = destination,
                            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
                        ) {
                            composable("onboarding") {
                                OnboardingScreen(
                                    onComplete = {
                                        navController.navigate("feed") {
                                            popUpTo("onboarding") { inclusive = true }
                                        }
                                    },
                                )
                            }
                            navigation(
                                startDestination = "feed_home",
                                route = "feed",
                            ) {
                                composable("feed_home") { backStackEntry ->
                                    val parentEntry = remember(backStackEntry) {
                                        navController.getBackStackEntry("feed")
                                    }
                                    val feedViewModel: FeedViewModel = hiltViewModel(parentEntry)
                                    settings?.let { s ->
                                        FeedScreen(
                                            settings = s,
                                            viewModel = feedViewModel,
                                            onPaperClick = { paper ->
                                                paperNavigationCache.put(paper)
                                                navController.navigate("detail/${paper.id}")
                                            },
                                            onAddCategories = {
                                                navController.navigate("add-categories")
                                            },
                                            onCategorySwitch = { code ->
                                                mainViewModel.setActiveCategory(code)
                                            },
                                            onNavigateToFilter = {
                                                navController.navigate("filter_screen")
                                            },
                                        )
                                    }
                                }
                                composable("filter_screen") { backStackEntry ->
                                    val parentEntry = remember(backStackEntry) {
                                        navController.getBackStackEntry("feed")
                                    }
                                    val feedViewModel: FeedViewModel = hiltViewModel(parentEntry)
                                    val feedState by feedViewModel.state.collectAsState()
                                    FilterScreen(
                                        fromDate = feedState.fromDate,
                                        toDate = feedState.toDate,
                                        sortOrder = feedState.sortOrder,
                                        onApply = { from, to, sort ->
                                            feedViewModel.setDateFilter(from, to)
                                            feedViewModel.setSortOrder(sort)
                                            navController.popBackStack()
                                        },
                                        onClear = {
                                            feedViewModel.clearDateFilter()
                                            feedViewModel.setSortOrder(com.paperstack.data.remote.SortOrder.SUBMITTED_DATE)
                                            navController.popBackStack()
                                        },
                                        onBack = { navController.popBackStack() },
                                    )
                                }
                            }
                            composable("saved") {
                                SavedScreen(
                                    onPaperClick = { paper ->
                                        paperNavigationCache.put(paper)
                                        navController.navigate("detail/${paper.id}")
                                    },
                                )
                            }
                            composable(
                                route = "detail/{paperId}",
                                arguments = listOf(navArgument("paperId") { type = NavType.StringType }),
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

@Composable
private fun PaperStackBottomNav(
    currentRoute: String?,
    onTabSelected: (String) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BottomNavTab(
                    label = "Feed",
                    isActive = currentRoute == "feed_home",
                    activeIcon = Icons.Filled.Home,
                    inactiveIcon = Icons.Outlined.Home,
                    onClick = { onTabSelected("feed") },
                )
                BottomNavTab(
                    label = "Saved",
                    isActive = currentRoute == "saved",
                    activeIcon = Icons.Filled.Bookmark,
                    inactiveIcon = Icons.Outlined.BookmarkBorder,
                    onClick = { onTabSelected("saved") },
                )
            }
        }
    }
}

@Composable
private fun BottomNavTab(
    label: String,
    isActive: Boolean,
    activeIcon: androidx.compose.ui.graphics.vector.ImageVector,
    inactiveIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    val color = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(Spacing.md))
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = if (isActive) activeIcon else inactiveIcon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}
