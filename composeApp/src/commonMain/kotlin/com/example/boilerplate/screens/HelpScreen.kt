package com.example.boilerplate.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import boilerplate.composeapp.generated.resources.Res
import boilerplate.composeapp.generated.resources.meu_icone
import com.example.boilerplate.theme.AppleRed
import org.jetbrains.compose.resources.painterResource

@Composable
fun HelpScreen() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background Glow
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Canvas(modifier = Modifier.fillMaxSize().blur(100.dp).alpha(0.12f)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(AppleRed, Color.Transparent),
                        center = center,
                        radius = size.maxDimension / 2
                    ),
                    radius = size.maxDimension / 1.5f,
                    center = center.copy(x = size.width, y = 0f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            Icon(
                painter = painterResource(Res.drawable.meu_icone),
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Color.Unspecified
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "App Documentation",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Everything you need to know",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(32.dp))
            
            Column(modifier = Modifier.fillMaxWidth()) {
                HelpSection(
                    title = "How to Download",
                    description = "1. Enter the name of a song or artist in the search bar.\n" +
                                "2. Click 'Search' or press Enter on your keyboard.\n" +
                                "3. Select your preferred format (MP3 or MP4).\n" +
                                "4. Choose video quality (for MP4).\n" +
                                "5. Click 'Confirm Download' to start."
                )

                HelpSection(
                    title = "Managing History",
                    description = "You can find all your downloads in the 'Music' and 'Videos' tabs. " +
                                "Use the search bar at the top of these lists to quickly find a specific file by its title."
                )
                
                HelpSection(
                    title = "Formats & Quality",
                    description = "• MP3: Optimized for audio with file size limits for speed.\n" +
                                "• MP4: Video format available in 720p and 1080p."
                )

                HelpSection(
                    title = "Theme & Appearance",
                    description = "The app features an Apple Music-inspired design. It automatically switches between Light and Dark modes based on your system settings."
                )
                
                HelpSection(
                    title = "Where are my files?",
                    description = "Downloads are saved in your system's Music folder under 'MyDownloaderApp'. You can open the folder directly from the app."
                )

                HelpSection(
                    title = "Engines",
                    description = "This app uses yt-dlp for downloading and FFmpeg for high-quality media processing."
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HorizontalDivider(modifier = Modifier.alpha(0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Developer: Daniel dos Santos Araujo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Company: Denenewton",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Version 1.1.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HelpSection(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
