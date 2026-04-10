package com.example.boilerplate.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.boilerplate.components.*
import com.example.boilerplate.models.SearchResult
import com.example.boilerplate.theme.AppleRed
import com.example.boilerplate.viewmodels.MusicDownloadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    viewModel: MusicDownloadViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var selectedFormat by remember { mutableStateOf("MP3") }
    var selectedQuality by remember { mutableStateOf("720p") }

    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Download Preview", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = state.error != null) {
                state.error?.let { ErrorBanner(message = it, onDismiss = { viewModel.resetState() }) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .alpha(if (state.isSearching) pulseAlpha else 1f),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    ResultHeader(state.result, state.isSearching)

                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(modifier = Modifier.alpha(0.5f))
                    Spacer(modifier = Modifier.height(20.dp))

                    if (state.result is SearchResult.Playlist && !state.isDownloading && !state.status.contains("Completed")) {
                        PlaylistItemsList(state.result as SearchResult.Playlist, viewModel)
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    DownloadActionSection(
                        state = state,
                        viewModel = viewModel,
                        format = selectedFormat,
                        quality = selectedQuality,
                        onFormatChange = { selectedFormat = it },
                        onQualityChange = { selectedQuality = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultHeader(result: SearchResult, isSearching: Boolean) {
    when (result) {
        is SearchResult.Playlist -> PlaylistHeader(result)
        is SearchResult.Single -> SingleItemHeader(result.metadata)
        SearchResult.Idle -> if (isSearching) {
            Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppleRed)
            }
        }
    }
}

@Composable
private fun DownloadActionSection(
    state: com.example.boilerplate.models.DownloadUiState,
    viewModel: MusicDownloadViewModel,
    format: String,
    quality: String,
    onFormatChange: (String) -> Unit,
    onQualityChange: (String) -> Unit
) {
    AnimatedContent(targetState = when {
        state.status.contains("Completed") -> 2
        state.isDownloading -> 1
        state.result !is SearchResult.Idle -> 0
        else -> -1
    }) { phase ->
        when (phase) {
            0 -> Column {
                Text("Download Options", style = MaterialTheme.typography.labelLarge, color = AppleRed)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormatChip("MP3", format == "MP3") { onFormatChange("MP3") }
                    FormatChip("MP4", format == "MP4") { onFormatChange("MP4") }
                }
                if (format == "MP4") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QualityChip("720p", quality == "720p") { onQualityChange("720p") }
                        QualityChip("1080p", quality == "1080p") { onQualityChange("1080p") }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { 
                        viewModel.startDownload(format, quality)
                    },
                    enabled = state.result !is SearchResult.Playlist || (state.result as SearchResult.Playlist).selectedIndices.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleRed)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (state.result is SearchResult.Playlist) 
                            "Download Selected (${(state.result as SearchResult.Playlist).selectedIndices.size})" 
                        else "Confirm Download", 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            1 -> DownloadProgressSection(state)
            2 -> Button(
                onClick = { viewModel.openDownloads() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) { Text("📁 Open Downloads Folder", fontWeight = FontWeight.Bold) }
        }
    }
}
