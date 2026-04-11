package com.example.boilerplate

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import boilerplate.composeapp.generated.resources.Res
import boilerplate.composeapp.generated.resources.meu_icone
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import com.example.boilerplate.di.initKoin
import com.example.boilerplate.downloader.BinaryManager
import com.example.boilerplate.theme.AppleRed
import org.jetbrains.compose.resources.painterResource

/**
 * Main entry point for the Desktop application.
 */
fun main() {
    // Initialize Koin Dependency Injection
    initKoin()

    application {
        setSingletonImageLoaderFactory { context ->
            ImageLoader.Builder(context)
                .components {
                    add(OkHttpNetworkFetcherFactory())
                }
                .build()
        }

        val windowState = rememberWindowState(width = 850.dp, height = 750.dp)

        Window(
            onCloseRequest = ::exitApplication,
            title = "Music Downloader",
            state = windowState,
            icon = painterResource(Res.drawable.meu_icone)
        ) {
            var isReady by remember { mutableStateOf(false) }
            var statusText by remember { mutableStateOf("Initializing components...") }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            var retryCount by remember { mutableStateOf(0) }

            LaunchedEffect(retryCount) {
                errorMessage = null
                try {
                    BinaryManager.ensureBinaries { status ->
                        statusText = status
                    }
                    isReady = true
                } catch (e: Exception) {
                    errorMessage = e.message ?: "Failed to initialize binaries"
                }
            }

            MaterialTheme(colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
                if (isReady) {
                    App()
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background Glow
                        Canvas(modifier = Modifier.fillMaxSize().blur(100.dp).alpha(0.15f)) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(AppleRed, Color.Transparent),
                                    center = center,
                                    radius = size.maxDimension / 2
                                ),
                                radius = size.maxDimension / 2,
                                center = center
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.meu_icone),
                                contentDescription = null,
                                modifier = Modifier.size(120.dp),
                                tint = Color.Unspecified
                            )
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            if (errorMessage != null) {
                                Text(
                                    text = "Initialization Error",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = AppleRed,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { retryCount++ },
                                    colors = ButtonDefaults.buttonColors(containerColor = AppleRed)
                                ) {
                                    Text("Retry")
                                }
                            } else {
                                LinearProgressIndicator(
                                    modifier = Modifier.width(200.dp),
                                    color = AppleRed,
                                    trackColor = AppleRed.copy(alpha = 0.1f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = statusText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
