package com.example.boilerplate

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

        val windowState = rememberWindowState(width = 700.dp, height = 750.dp)

        Window(
            onCloseRequest = ::exitApplication,
            title = "Music Downloader",
            state = windowState,
            icon = painterResource(Res.drawable.meu_icone)
        ) {
            var isReady by remember { mutableStateOf(false) }
            var statusText by remember { mutableStateOf("Initializing...") }

            LaunchedEffect(Unit) {
                BinaryManager.ensureBinaries { status ->
                    statusText = status
                }
                isReady = true
            }

            if (isReady) {
                App()
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                    Text(text = statusText, modifier = Modifier.align(Alignment.BottomCenter))
                }
            }
        }
    }
}
