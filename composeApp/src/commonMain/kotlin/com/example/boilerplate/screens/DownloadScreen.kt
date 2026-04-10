package com.example.boilerplate.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.boilerplate.components.*
import com.example.boilerplate.models.SearchResult
import com.example.boilerplate.theme.AppleRed
import com.example.boilerplate.viewmodels.MusicDownloadViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DownloadScreen(
    viewModel: MusicDownloadViewModel = koinViewModel(),
    onNavigateToPreview: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    LaunchedEffect(state.result) {
        if (state.result !is SearchResult.Idle) {
            onNavigateToPreview()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Gradient Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Canvas(modifier = Modifier.fillMaxSize().blur(100.dp).alpha(0.15f)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AppleRed, Color.Transparent),
                        center = center,
                        radius = size.maxDimension / 2
                    ),
                    radius = size.maxDimension / 1.5f,
                    center = center.copy(y = 0f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            AppHeader(modifier = Modifier.animateContentSize())

            Spacer(modifier = Modifier.height(40.dp))

            AnimatedVisibility(
                visible = state.error != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                state.error?.let { ErrorBanner(message = it, onDismiss = { viewModel.resetState() }) }
            }

            SearchSection(
                query = state.query,
                onQueryChange = { viewModel.onQueryChange(it) },
                isSearching = state.isSearching,
                searchType = state.searchType,
                isDownloading = state.isDownloading,
                onSearch = { forcePlaylist ->
                    keyboardController?.hide()
                    viewModel.searchMetadata(forcePlaylist = forcePlaylist)
                }
            )

            Spacer(modifier = Modifier.height(48.dp))

            EmptyStatePlaceholder(isSearching = state.isSearching)
        }
    }
}

@Composable
private fun SearchSection(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearching: Boolean,
    searchType: com.example.boilerplate.models.SearchType,
    isDownloading: Boolean,
    onSearch: (Boolean) -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "What would you like to hear?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            MusicSearchBar(
                value = query,
                onValueChange = onQueryChange,
                placeholder = "Song title, artist name or URL...",
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSearching && !isDownloading,
                onClear = { onQueryChange("") },
                onSearchAction = { onSearch(false) }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SearchButton(
                    text = "Single",
                    icon = Icons.Default.MusicNote,
                    color = AppleRed,
                    isLoading = isSearching && searchType == com.example.boilerplate.models.SearchType.SINGLE,
                    enabled = query.isNotBlank() && !isSearching && !isDownloading,
                    onClick = { onSearch(false) },
                    modifier = Modifier.weight(1.1f)
                )

                SearchButton(
                    text = "Playlist",
                    icon = Icons.AutoMirrored.Filled.PlaylistPlay,
                    color = Color.DarkGray,
                    isLoading = isSearching && searchType == com.example.boilerplate.models.SearchType.PLAYLIST,
                    enabled = query.isNotBlank() && !isSearching && !isDownloading,
                    onClick = { onSearch(true) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SearchButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = color.copy(alpha = 0.3f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
        } else {
            Icon(icon, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun EmptyStatePlaceholder(isSearching: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 40.dp)
    ) {
        if (!isSearching) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.CloudDownload,
                        null,
                        modifier = Modifier.size(40.dp).alpha(alpha),
                        tint = AppleRed
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Ready for your next favorite track?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Paste a link or search for music and playlists to start your collection.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            // Loading State within placeholder
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LinearProgressIndicator(
                    modifier = Modifier.width(120.dp).clip(CircleShape),
                    color = AppleRed,
                    trackColor = AppleRed.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Searching the musical universe...",
                    style = MaterialTheme.typography.labelLarge,
                    color = AppleRed,
                    modifier = Modifier.alpha(alpha)
                )
            }
        }
    }
}
