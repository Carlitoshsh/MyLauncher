# MyLauncher

A minimalist Android home screen launcher built with Jetpack Compose. MyLauncher replaces your default launcher with a clean, distraction-free home screen featuring a digital clock, wallpaper support, and up to 5 customizable app shortcuts.

## Features

- **Home Screen** — Digital clock (date + time), system wallpaper display, and up to 5 app shortcuts
  - Portrait mode: grid layout; landscape mode: row layout
  - Long-press shortcuts for app actions (shortcuts, app info, open)
- **App Drawer** — Full alphabetical list of installed apps with shortcut toggle support
- **Settings** — Toggle 12/24-hour time format, set as default launcher

## Screenshots

> _Add screenshots here._

## Requirements

- Android 10 (API 29) or higher
- Android Studio Meerkat or newer

## Tech Stack

| Category | Library / Tool |
|---|---|
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, StateFlow, Coroutines |
| Persistence | Room 2.7, DataStore Preferences 1.1 |
| Networking | Retrofit 2.12, OkHttp 4.10, Moshi 1.15 |
| Image Loading | Coil 2.7 |
| Navigation | Compose Navigation 2.8 |
| Permissions | Accompanist Permissions 0.37 |
| Camera | CameraX 1.5 |
| Location | Play Services Location 21.3 |
| Build | Kotlin 2.2, KSP, Gradle (Kotlin DSL) |

## Project Structure

```
app/src/main/
├── data/
│   ├── AppDataSource.kt          # Fetches installed apps via PackageManager
│   ├── ShortcutsDataStore.kt     # Persists selected shortcuts
│   └── SettingsDataStore.kt      # Persists user preferences
├── model/
│   ├── AppInfo.kt                # Domain model for installed apps
│   └── ShortcutOption.kt         # Data class for context menu actions
├── ui/
│   ├── screens/
│   │   ├── HomeScreen.kt         # Clock, shortcuts, wallpaper
│   │   ├── AppDrawerScreen.kt    # App list with edit mode
│   │   └── SettingsScreen.kt     # Preferences
│   ├── components/
│   │   └── Clock.kt              # Reusable digital clock
│   └── theme/
│       ├── Color.kt
│       ├── Type.kt
│       └── Theme.kt
├── viewmodel/
│   ├── HomeViewModel.kt
│   ├── AppDrawerViewModel.kt
│   ├── HomeViewModelFactory.kt
│   └── AppDrawerViewModelFactory.kt
└── MainActivity.kt
```

## Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/Carlitoshsh/MyLauncher.git
   ```

2. **Open in Android Studio** — Open the project root directory.

3. **Build and run** — Select a device or emulator (API 29+) and press **Run**.

4. **Set as default launcher** — On first launch, go to **Settings → Set as default launcher** and follow the system prompt.

## Permissions

| Permission | Purpose |
|---|---|
| `INTERNET` | General connectivity |
| `READ_MEDIA_IMAGES` | Display system wallpaper (Android 13+) |
| `READ_EXTERNAL_STORAGE` | Display system wallpaper (Android 12 and below) |
| `READ_WALLPAPER_INTERNAL` | System wallpaper access |

## License

This project is open source. See [LICENSE](LICENSE) for details.
