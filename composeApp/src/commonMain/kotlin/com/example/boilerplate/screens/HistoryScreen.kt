package com.example.boilerplate.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.boilerplate.components.HistoryItem
import com.example.boilerplate.components.ItemDetailScreen
import com.example.boilerplate.components.MusicSearchBar
import com.example.boilerplate.database.DownloadedItem
import com.example.boilerplate.theme.AppleRed
import com.example.boilerplate.viewmodels.MusicDownloadViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Screen that displays the history of downloaded media (Music or Videos).
 */
@Composable
fun HistoryList(isMusic: Boolean, viewModel: MusicDownloadViewModel = koinViewModel()) {
    val items by (if (isMusic) viewModel.musicList else viewModel.videoList).collectAsState()
    var selectedItem by remember { mutableStateOf<DownloadedItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter { it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Glow
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Canvas(modifier = Modifier.fillMaxSize().blur(100.dp).alpha(0.1f)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(if (isMusic) AppleRed else Color.Cyan, Color.Transparent),
                        center = center,
                        radius = size.maxDimension / 2
                    ),
                    radius = size.maxDimension / 1.5f,
                    center = center.copy(y = size.height)
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Title
            Column(modifier = Modifier.padding(top = 32.dp, start = 24.dp, end = 24.dp, bottom = 8.dp)) {
                Text(
                    text = if (isMusic) "My Music" else "My Videos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${items.size} files downloaded",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            MusicSearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = "Search in your library...",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                onClear = { searchQuery = "" }
            )

            AnimatedContent(
                targetState = filteredItems.isEmpty(),
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "HistoryContentTransition"
            ) { isEmpty ->
                if (isEmpty) {
                    EmptyHistoryPlaceholder(isMusic, searchQuery)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 32.dp, start = 16.dp, end = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredItems, key = { it.id }) { item ->
                            HistoryItem(
                                item = item,
                                onClick = { selectedItem = item },
                                onPlay = { viewModel.openFile(item.filePath) }
                            )
                        }
                    }
                }
            }
        }

        // Detail Overlay
        AnimatedVisibility(
            visible = selectedItem != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            selectedItem?.let { item ->
                ItemDetailScreen(
                    item = item,
                    onBack = { selectedItem = null },
                    onPlay = { viewModel.openFile(item.filePath) },
                    onShare = { viewModel.shareFile(item.filePath) },
                    onDelete = {
                        viewModel.deleteItem(item)
                        selectedItem = null
                    }
                )
            }
        }
    }
}

@Composable
private fun EmptyHistoryPlaceholder(isMusic: Boolean, searchQuery: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isMusic) Icons.Default.MusicNote else Icons.Default.Videocam,
            contentDescription = null,
            modifier = Modifier.size(64.dp).alpha(0.1f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (searchQuery.isEmpty()) {
                "Your ${if (isMusic) "music" else "video"} history is empty"
            } else {
                "No matches found for \"$searchQuery\""
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(0.5f)
        )
    }
}
