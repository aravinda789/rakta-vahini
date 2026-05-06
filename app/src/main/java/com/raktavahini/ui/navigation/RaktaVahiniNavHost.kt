package com.raktavahini.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.raktavahini.ui.screens.DonationHistoryScreen
import com.raktavahini.ui.screens.HomeScreen
import com.raktavahini.ui.screens.RegisterDonorScreen
import com.raktavahini.ui.screens.DonorProfileScreen
import com.raktavahini.ui.screens.history.HistoryListScreen
import com.raktavahini.ui.screens.profile.ProfileScreen
import com.raktavahini.ui.viewmodel.DonorProfileViewModel
import com.raktavahini.ui.viewmodel.HomeViewModel

@Composable
fun RaktabhVahiniNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel = hiltViewModel(),
    onLanguageChange: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 300 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -300 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -300 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { 300 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(
            route = Screen.Home.route,
            enterTransition = {
                fadeIn(animationSpec = tween(400))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(400))
            }
        ) {
            HomeScreen(
                viewModel = homeViewModel,
                onDonorClick = { donorId ->
                    navController.navigate(Screen.DonorProfile.createRoute(donorId))
                },
                onLanguageChange = onLanguageChange
            )
        }

        composable(
            route = "profile",
            enterTransition = {
                slideInVertically(initialOffsetY = { 300 }) + fadeIn()
            },
            exitTransition = {
                slideOutVertically(targetOffsetY = { -300 }) + fadeOut()
            }
        ) {
            ProfileScreen(
                onDonorClick = { donorId ->
                    navController.navigate(Screen.DonorProfile.createRoute(donorId))
                }
            )
        }

        composable(
            route = "history_list",
            enterTransition = {
                slideInVertically(initialOffsetY = { 300 }) + fadeIn()
            },
            exitTransition = {
                slideOutVertically(targetOffsetY = { -300 }) + fadeOut()
            }
        ) {
            HistoryListScreen(
                onDonorClick = { donorId ->
                    navController.navigate(Screen.DonorProfile.createRoute(donorId))
                }
            )
        }

        composable(
            route = Screen.DonorProfile.route,
            arguments = listOf(navArgument("donorId") { type = NavType.LongType }),
            enterTransition = {
                slideInVertically(initialOffsetY = { 300 }) + fadeIn()
            },
            exitTransition = {
                slideOutVertically(targetOffsetY = { -300 }) + fadeOut()
            },
            popEnterTransition = {
                slideInVertically(initialOffsetY = { -300 }) + fadeIn()
            },
            popExitTransition = {
                slideOutVertically(targetOffsetY = { 300 }) + fadeOut()
            }
        ) { backStackEntry ->
            val donorId = backStackEntry.arguments?.getLong("donorId") ?: 0L
            val viewModel: DonorProfileViewModel = hiltViewModel()
            remember(donorId) { viewModel.loadDonor(donorId) }

            DonorProfileScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RegisterDonor.route,
            enterTransition = {
                slideInVertically(initialOffsetY = { 300 }) + fadeIn()
            },
            exitTransition = {
                slideOutVertically(targetOffsetY = { -300 }) + fadeOut()
            }
        ) {
            val viewModel: DonorProfileViewModel = hiltViewModel()
            val context = LocalContext.current

            RegisterDonorScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onRegistered = { donorId ->
                    navController.popBackStack()
                    navController.navigate(Screen.DonorProfile.createRoute(donorId))
                },
                context = context
            )
        }

        composable(
            route = Screen.DonationHistory.route,
            arguments = listOf(navArgument("donorId") { type = NavType.LongType }),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { 300 }) + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -300 }) + fadeOut()
            }
        ) { backStackEntry ->
            val donorId = backStackEntry.arguments?.getLong("donorId") ?: 0L
            val viewModel: DonorProfileViewModel = hiltViewModel()
            remember(donorId) { viewModel.loadDonor(donorId) }

            DonationHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}