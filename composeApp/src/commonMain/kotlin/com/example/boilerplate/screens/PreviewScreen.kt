package com.example.boilerplate.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.boilerplate.components.*
import com.example.boilerplate.models.SearchResult
import com.example.boilerplate.theme.AppleRed
import com.example.boilerplate.viewmodels.MusicDownloadViewModel

@Composable
fun PreviewScreen(
    viewModel: MusicDownloadViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var selectedFormat by remember { mutableStateOf("MP3") }
    var selectedQuality by remember { mutableStateOf("720p") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Modern Background Glow
        Canvas(modifier = Modifier.fillMaxSize().blur(100.dp).alpha(0.12f)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AppleRed, Color.Transparent),
                    center = center,
                    radius = size.maxDimension / 2.5f
                ),
                radius = size.maxDimension / 2.5f,
                center = center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AppleRed)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Download Preview",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            AnimatedVisibility(visible = state.error != null) {
                state.error?.let { ErrorBanner(message = it, onDismiss = { viewModel.resetState() }) }
            }

            // Main Content Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                    .border(
                        1.dp, 
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), 
                        RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
            ) {
                Column {
                    ResultHeader(state.result, state.isSearching)

                    if (state.result !is SearchResult.Idle) {
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(modifier = Modifier.alpha(0.1f))
                        Spacer(modifier = Modifier.height(24.dp))

                        if (state.result is SearchResult.Playlist && !state.isDownloading && !state.status.contains("Completed")) {
                            PlaylistItemsList(state.result as SearchResult.Playlist, viewModel)
                            Spacer(modifier = Modifier.height(24.dp))
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
}

@Composable
private fun ResultHeader(result: SearchResult, isSearching: Boolean) {
    when (result) {
        is SearchResult.Playlist -> PlaylistHeader(result)
        is SearchResult.Single -> SingleItemHeader(result.metadata)
        SearchResult.Idle -> if (isSearching) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = AppleRed, strokeWidth = 3.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Analyzing content...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
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
        else -> 0
    }) { phase ->
        when (phase) {
            0 -> Column {
                Text(
                    "Select Preferences", 
                    style = MaterialTheme.typography.titleMedium, 
                    color = AppleRed,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormatChip("Audio (MP3)", format == "MP3") { onFormatChange("MP3") }
                    FormatChip("Video (MP4)", format == "MP4") { onFormatChange("MP4") }
                }
                
                if (format == "MP4") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QualityChip("HD 720p", quality == "720p") { onQualityChange("720p") }
                        QualityChip("FHD 1080p", quality == "1080p") { onQualityChange("1080p") }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { viewModel.startDownload(format, quality) },
                    enabled = state.result !is SearchResult.Playlist || (state.result as SearchResult.Playlist).selectedIndices.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppleRed)
                ) {
                    Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        if (state.result is SearchResult.Playlist) 
                            "Download ${(state.result as SearchResult.Playlist).selectedIndices.size} Items" 
                        else "Download Now", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
            1 -> DownloadProgressSection(state)
            2 -> Button(
                onClick = { viewModel.openDownloads() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) { 
                Icon(Icons.Default.FolderOpen, null)
                Spacer(modifier = Modifier.width(10.dp))
                Text("Open Downloads Folder", fontWeight = FontWeight.Bold) 
            }
        }
    }
}
