package com.raktavahini

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.raktavahini.data.LanguageManager
import com.raktavahini.ui.navigation.RaktabhVahiniNavHost
import com.raktavahini.ui.screens.LanguageSelectionScreen
import com.raktavahini.ui.screens.RegisterDonorScreen
import com.raktavahini.ui.screens.SplashScreen
import com.raktavahini.ui.theme.*
import com.raktavahini.ui.viewmodel.DonorProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource

sealed class BottomNavItem(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    object Home : BottomNavItem("home", R.string.bottom_search, Icons.Default.Search)
    object Profile : BottomNavItem("profile", R.string.bottom_profile, Icons.Default.Person)
    object History : BottomNavItem("history_list", R.string.bottom_history, Icons.Default.History)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var languageManager: LanguageManager

    override fun attachBaseContext(newBase: Context) {
        val manager = com.raktavahini.data.LanguageManager(newBase)
        val context = com.raktavahini.util.LocaleHelper.updateContext(newBase, manager.getSelectedLanguage())
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languageManager = LanguageManager(this)

        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val isOnboarded = prefs.getBoolean("onboarded", false)

        setContent {
            RaktaVahiniTheme {
                var showSplash by remember { mutableStateOf(true) }
                var showLanguageScreen by remember { mutableStateOf(!languageManager.isLanguageSelected()) }
                var showRegistration by remember { mutableStateOf(!isOnboarded && languageManager.isLanguageSelected()) }

                val stateFlow = when {
                    showSplash -> "splash"
                    showLanguageScreen -> "language"
                    showRegistration -> "registration"
                    else -> "main"
                }

                AnimatedContent(
                    targetState = stateFlow,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) togetherWith
                                fadeOut(animationSpec = tween(500))
                    },
                    label = "navigation"
                ) { state ->
                    when (state) {
                        "splash" -> SplashScreen(
                            onNavigate = {
                                showSplash = false
                            }
                        )
                        "language" -> LanguageSelectionScreen(
                            languageManager = languageManager,
                            onLanguageSelected = {
                                showLanguageScreen = false
                                if (!isOnboarded) {
                                    showRegistration = true
                                }
                                (this@MainActivity as android.app.Activity).recreate()
                            }
                        )
                        "registration" -> {
                             val viewModel: DonorProfileViewModel = hiltViewModel()
                             RegisterDonorScreen(
                                 viewModel = viewModel,
                                 onBack = {
                                     showRegistration = false
                                     prefs.edit().putBoolean("onboarded", true).apply()
                                 },
                                 onRegistered = { donorId ->
                                     showRegistration = false
                                     prefs.edit().putBoolean("onboarded", true).apply()
                                 },
                                 context = this@MainActivity
                             )
                         }
                        else -> MainAppContent(
                            onLanguageChange = {
                                languageManager.setLanguage("")
                                showLanguageScreen = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainAppContent(onLanguageChange: () -> Unit = {}) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf(BottomNavItem.Home, BottomNavItem.Profile, BottomNavItem.History)
    val showBottomBar = currentDestination?.route in listOf("home", "profile", "history_list")

    Scaffold(
        containerColor = Background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                ) {
                    NavigationBar(
                        containerColor = Surface,
                        modifier = Modifier
                            .height(72.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .shadow(12.dp, RoundedCornerShape(24.dp)),
                        tonalElevation = 8.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                            NavigationBarItem(
                                icon = {
                                    AnimatedIcon(
                                        targetIcon = item.icon,
                                        contentDescription = stringResource(item.titleRes),
                                        isSelected = selected
                                    )
                                },
                                label = { 
                                    Text(
                                        stringResource(item.titleRes), 
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    ) 
                                },
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Primary,
                                    selectedTextColor = Primary,
                                    unselectedIconColor = OnSurfaceVariant.copy(alpha = 0.5f),
                                    unselectedTextColor = OnSurfaceVariant.copy(alpha = 0.5f),
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            RaktabhVahiniNavHost(navController = navController, onLanguageChange = onLanguageChange)
        }
    }
}

@Composable
fun AnimatedIcon(
    targetIcon: ImageVector,
    contentDescription: String?,
    isSelected: Boolean
) {
    AnimatedContent(
        targetState = isSelected,
        transitionSpec = {
            scaleIn(initialScale = 0.8f) + fadeIn() togetherWith
                    scaleOut(targetScale = 0.8f) + fadeOut()
        },
        label = "icon"
    ) { selected ->
        Icon(
            imageVector = targetIcon,
            contentDescription = contentDescription,
            modifier = Modifier.size(if (selected) 28.dp else 24.dp),
            tint = if (selected) Primary else OnSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}