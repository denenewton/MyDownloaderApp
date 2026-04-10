package com.example.boilerplate.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import boilerplate.composeapp.generated.resources.Res
import boilerplate.composeapp.generated.resources.meu_icone
import coil3.compose.AsyncImage
import com.example.boilerplate.models.DownloadUiState
import com.example.boilerplate.models.SearchResult
import com.example.boilerplate.theme.AppleRed
import com.example.boilerplate.viewmodels.MusicDownloadViewModel
import org.jetbrains.compose.resources.painterResource

@Composable
fun AppHeader(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Music Downloader", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Icon(painter = painterResource(Res.drawable.meu_icone), contentDescription = null, modifier = Modifier.size(50.dp), tint = Color.Unspecified)
        }
        Text(text = "Desktop Edition", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
fun ErrorBanner(message: String, onDismiss: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp), shape = RoundedCornerShape(12.dp), color = AppleRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, AppleRed.copy(alpha = 0.2f))) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ErrorOutline, null, tint = AppleRed, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = message, style = MaterialTheme.typography.bodySmall, color = AppleRed, modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, "Dismiss", tint = AppleRed, modifier = Modifier.size(16.dp)) }
        }
    }
}

@Composable
fun SingleItemHeader(metadata: com.example.boilerplate.downloader.MediaMetadata) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
            AsyncImage(model = metadata.thumbnailUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = metadata.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("⏱ ${metadata.duration}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("📦 ${metadata.fileSize}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun PlaylistHeader(playlist: SearchResult.Playlist) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(modifier = Modifier.size(60.dp), shape = RoundedCornerShape(12.dp), color = AppleRed.copy(alpha = 0.1f)) {
            Icon(Icons.Default.PlaylistPlay, null, modifier = Modifier.padding(12.dp), tint = AppleRed)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = playlist.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${playlist.items.size} tracks found", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun PlaylistItemsList(playlist: SearchResult.Playlist, viewModel: MusicDownloadViewModel) {
    Column(modifier = Modifier.heightIn(max = 350.dp)) {
        val allSelected = playlist.selectedIndices.size == playlist.items.size
        Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { viewModel.toggleAllPlaylistItems(!allSelected) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = allSelected, onCheckedChange = { viewModel.toggleAllPlaylistItems(it) }, colors = CheckboxDefaults.colors(checkedColor = AppleRed))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = if (allSelected) "Deselect All" else "Select All", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = if (allSelected) AppleRed else MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.weight(1f))
            Text("${playlist.selectedIndices.size} selected", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp).alpha(0.3f))
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            itemsIndexed(playlist.items) { index, item ->
                val isSelected = playlist.selectedIndices.contains(index)
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(if (isSelected) AppleRed.copy(alpha = 0.05f) else Color.Transparent).clickable { viewModel.togglePlaylistItem(index) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isSelected, onCheckedChange = { viewModel.togglePlaylistItem(index) }, colors = CheckboxDefaults.colors(checkedColor = AppleRed))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(item.title, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    Text(item.duration, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun DownloadProgressSection(state: DownloadUiState) {
    Column {
        val animatedProgress by animateFloatAsState(targetValue = state.progress, animationSpec = tween(durationMillis = 500, easing = LinearEasing))
        Row(verticalAlignment = Alignment.Bottom) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = state.status, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = AppleRed, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (state.result is SearchResult.Playlist) {
                    val playlist = state.result as SearchResult.Playlist
                    Text("Playlist progress: ${playlist.currentDownloadIndex + 1}/${playlist.selectedIndices.size}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            Text(text = "${(state.progress * 100).toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = AppleRed)
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(progress = { animatedProgress }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)), color = AppleRed, trackColor = AppleRed.copy(alpha = 0.1f))
        if (state.speed.isNotEmpty() || state.eta.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🚀 ${state.speed}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 11.sp)
                Text("⏳ ${state.eta} remaining", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 11.sp)
                Text("📦 ${state.downloadedSize}", style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun FormatChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label, modifier = Modifier.padding(horizontal = 8.dp)) }, shape = RoundedCornerShape(8.dp), colors = FilterChipDefaults.filterChipColors(selectedContainerColor = AppleRed, selectedLabelColor = Color.White, containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)), border = null)
}

@Composable
fun QualityChip(label: String, selected: Boolean, onClick: () -> Unit) {
    AssistChip(onClick = onClick, label = { Text(label) }, shape = RoundedCornerShape(8.dp), colors = AssistChipDefaults.assistChipColors(containerColor = if (selected) AppleRed.copy(alpha = 0.1f) else Color.Transparent, labelColor = if (selected) AppleRed else MaterialTheme.colorScheme.onSurface), border = BorderStroke(width = 1.dp, color = if (selected) AppleRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
}
