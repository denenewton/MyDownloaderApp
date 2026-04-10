# Boilerplate Media Downloader (Compose Multiplatform)

A modern, cross-platform media downloader built with **Kotlin Multiplatform** and **Compose Multiplatform**, targeting Android and Desktop (JVM). It allows users to search, preview, and download music (MP3) or videos (MP4) using `yt-dlp` and `FFmpeg` engines.

## 🚀 Key Features

*   **Search & Preview**: Search for any song or artist. The app fetches metadata (Title, Duration, Thumbnail, File Size) before downloading, allowing you to confirm the content.
*   **Multiple Formats**: Download as high-quality **MP3** (audio) or **MP4** (video).
*   **Quality Selection**: Choose between **720p** and **1080p** for video downloads.
*   **Download History**: Local database (Room/Sqlite) keeps track of your downloads, separated by Music and Videos tabs.
*   **In-App Management**: Play downloaded files directly with your system's default player or delete them (both from history and disk).
*   **Real-time Progress**: Visual feedback for searching, downloading, and converting processes.

## 🛠 Tech Stack

*   **UI**: Compose Multiplatform
*   **Architecture**: MVVM (Model-View-ViewModel)
*   **Dependency Injection**: Koin
*   **Local Database**: Room (KMP)
*   **Image Loading**: Coil3
*   **Engines**: 
    *   `yt-dlp`: For metadata extraction and downloading.
    *   `FFmpeg`: For high-quality media conversion and merging.
*   **Coroutines**: For asynchronous operations.

## 📂 Project Structure

*   `composeApp/src/commonMain`: Shared logic, ViewModels, Database, and UI components.
*   `composeApp/src/androidMain`: Android-specific implementations (e.g., using System Download Manager).
*   `composeApp/src/jvmMain`: Desktop-specific implementations (Process management for `yt-dlp`).

## ⚙️ How it works

1.  **Search Phase**: When you type a query, the `MusicDownloadViewModel` calls `fetchMetadata`. It uses `yt-dlp` to get only the info without downloading the bits.
2.  **Preview Phase**: A result card appears with the thumbnail and size. You select your preferred format and quality.
3.  **Download Phase**: Clicking "Confirm" starts the actual download. The app handles the command-line execution of the engine and reports progress back to the UI.
4.  **Storage**: Files are saved to `~/Music/MyDownloaderApp` (Desktop) or the standard Downloads folder (Android).

## 🛠 Build and Run

### Android
- macOS/Linux: `./gradlew :composeApp:assembleDebug`
- Windows: `.\gradlew.bat :composeApp:assembleDebug`

### Desktop (JVM)
- macOS/Linux: `./gradlew :composeApp:run`
- Windows: `.\gradlew.bat :composeApp:run`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)
