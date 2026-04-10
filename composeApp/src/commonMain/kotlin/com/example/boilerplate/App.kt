package com.example.boilerplate

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.boilerplate.screens.DownloadScreen
import com.example.boilerplate.screens.HelpScreen
import com.example.boilerplate.screens.HistoryList
import com.example.boilerplate.screens.PreviewScreen
import com.example.boilerplate.theme.appTypography
import com.example.boilerplate.theme.AppleDarkColorScheme
import com.example.boilerplate.theme.AppleLightColorScheme
import com.example.boilerplate.theme.AppleRed
import com.example.boilerplate.viewmodels.MusicDownloadViewModel
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main application entry point.
 * Handles theme switching and bottom navigation between screens with smooth animations.
 */
@Composable
fun App() {
    KoinContext {
        val isDark = isSystemInDarkTheme()

        val colorScheme = remember(isDark) {
            if (isDark) AppleDarkColorScheme else AppleLightColorScheme
        }

        MaterialTheme(
            typography = appTypography(),
            colorScheme = colorScheme
        ) {
            var selectedTabIndex by remember { mutableIntStateOf(0) }
            var showPreview by remember { mutableStateOf(false) }
            
            val viewModel: MusicDownloadViewModel = koinViewModel()

            Scaffold(
                bottomBar = {
                    if (!showPreview) {
                        AppBottomNavigation(
                            selectedTabIndex = selectedTabIndex,
                            onTabSelected = { selectedTabIndex = it },
                            isDark = isDark
                        )
                    }
                }
            ) { padding ->
                Box(modifier = Modifier.padding(padding).fillMaxSize()) {

                    if (showPreview) {
                        PreviewScreen(
                            viewModel = viewModel,
                            onBack = { 
                                showPreview = false 
                                viewModel.resetState(clearQuery = true)
                            }
                        )
                    } else {
                        AnimatedContent(
                            targetState = selectedTabIndex,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220, easing = LinearOutSlowInEasing)) togetherWith 
                                fadeOut(animationSpec = tween(220, easing = FastOutLinearInEasing))
                            },
                            label = "TabTransition"
                        ) { targetIndex ->
                            when (targetIndex) {
                                0 -> DownloadScreen(
                                    viewModel = viewModel,
                                    onNavigateToPreview = { showPreview = true }
                                )
                                1 -> HistoryList(isMusic = true)
                                2 -> HistoryList(isMusic = false)
                                3 -> HelpScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppBottomNavigation(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    isDark: Boolean
) {

    val navigationItems = remember {
        listOf(
            NavigationItem("Download", Icons.Default.Download, Icons.Outlined.Download),
            NavigationItem("Music", Icons.Default.MusicNote, Icons.Outlined.MusicNote),
            NavigationItem("Videos", Icons.Default.Videocam, Icons.Outlined.Videocam),
            NavigationItem("About", Icons.Default.Info, Icons.Outlined.Info)
        )
    }

    Column {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.1f)
        )
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = 0.dp,
            modifier = Modifier.height(64.dp)
        ) {
            navigationItems.forEachIndexed { index, item ->
                val isSelected = selectedTabIndex == index
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(index) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppleRed,
                        selectedTextColor = AppleRed,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

private data class NavigationItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)
