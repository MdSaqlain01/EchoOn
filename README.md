# EchoOn

<div align="center">

![EchoOn Logo](app/src/main/res/drawable/ic_launcher.xml)

**A modern, privacy-first language translation app for Android**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Compose-2024.12.01-blue.svg)](https://developer.android.com/jetpack/compose)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-26-green.svg)](https://developer.android.com/studio/releases/platforms)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## 📱 Overview

**EchoOn** is a comprehensive language translation application for Android that provides seamless translation across three primary modes: **Write** (text), **Hear** (voice), and **See** (camera). Built with modern Android development practices, EchoOn prioritizes user privacy, performance, and a beautiful user experience.

### Key Features

- ✍️ **Text Translation** - Translate text between multiple languages with auto-detection
- 🎤 **Voice Translation** - Real-time speech-to-text translation with text-to-speech playback
- 📷 **Camera Translation** - On-device OCR with instant translation overlay (privacy-first, no images sent to servers)
- 🌓 **Dark/Light Mode** - System-aware theme switching with instant mode changes
- 🔐 **User Authentication** - Secure account creation and login with Supabase backend
- 📚 **Translation History** - Save and access your translation history (optional cloud sync)
- 🎨 **Modern UI** - Beautiful Material Design 3 interface with glassmorphism effects and smooth animations
- 🌍 **Multi-language Support** - Support for English, Spanish, French, Hindi, Urdu, and more

---

## 🏗️ Architecture

### Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Navigation**: Jetpack Navigation Compose
- **State Management**: Compose State, DataStore
- **Networking**: OkHttp
- **Coroutines**: Kotlin Coroutines for async operations
- **OCR**: Google ML Kit Text Recognition (on-device)
- **Backend**: Supabase (authentication & optional history)
- **Translation APIs**:
  - Google Cloud Translation API (optional, highest quality)
  - LibreTranslate (free, open-source)
  - MyMemory (free fallback)

### Project Structure

```
app/src/main/java/com/echoon/app/
├── MainActivity.kt                 # App entry point
├── EchoOnApp.kt                   # Application class
├── ui/
│   ├── EchoOnApp.kt               # Main navigation composable
│   ├── AnimatedComponents.kt      # Reusable animated UI components
│   ├── PendingWriteText.kt        # State holder for navigation
│   ├── home/
│   │   └── HomeRoute.kt           # Home screen with language selection
│   ├── write/
│   │   └── WriteRoute.kt          # Text translation screen
│   ├── hear/
│   │   └── HearRoute.kt           # Voice translation screen
│   ├── see/
│   │   └── SeeRoute.kt            # Camera translation screen
│   ├── history/
│   │   └── HistoryRoute.kt        # Translation history screen
│   ├── settings/
│   │   └── SettingsRoute.kt       # Settings and preferences
│   ├── login/
│   │   └── LoginRoute.kt         # Authentication screen
│   └── theme/
│       ├── Theme.kt               # Material3 theme configuration
│       └── ColorExtensions.kt    # Color utility extensions
├── services/
│   ├── TranslationService.kt      # Translation API integration
│   └── SupabaseHistoryService.kt # History sync service
└── preferences/
    ├── ThemePreferences.kt       # Theme mode persistence
    └── AuthRepository.kt          # Authentication repository
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK 17** or later
- **Android SDK** with API level 26+ (Android 8.0+)
- **Gradle** 8.0+ (included with Android Studio)

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd "Language translator app"
   ```

2. **Set up environment variables**
   
   Copy `.env.example` to `.env` in the project root:
   ```bash
   cp .env.example .env
   ```
   
   Edit `.env` and add your configuration:
   ```env
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your-anon-key
   GOOGLE_TRANSLATE_API_KEY=your-google-api-key  # Optional
   ```

3. **Get API Keys** (if needed)

   **Supabase** (Required for authentication):
   - Create a project at [supabase.com](https://supabase.com)
   - Copy your project URL and anon key from Project Settings → API
   
   **Google Translate API** (Optional, for better accuracy):
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a project or select existing
   - Enable **Cloud Translation API**
   - Create an API key in **APIs & Services → Credentials**
   - Add the key to `.env` as `GOOGLE_TRANSLATE_API_KEY`

4. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the project directory
   - Wait for Gradle sync to complete

5. **Build and Run**
   - Connect an Android device or start an emulator (API 26+)
   - Click "Run" or press `Shift+F10`
   - The app will build and install on your device

---

## 📖 Usage

### First Launch

1. **Create an Account**
   - On first launch, you'll see the login screen
   - Tap "Create account"
   - Enter a username and password (minimum 6 characters)
   - Your account is created and you're automatically logged in

2. **Navigate the App**
   - **Home**: Main screen with language selection and quick access to all modes
   - **Write**: Type or paste text, select languages, and translate
   - **Hear**: Tap the microphone, speak, and hear the translation
   - **See**: Point your camera at text for instant OCR and translation
   - **History**: View your past translations
   - **Settings**: Change theme, manage account, and more

### Translation Modes

#### ✍️ Write Mode
1. Navigate to Write from the home screen
2. Select source and target languages (or use "Auto" for detection)
3. Type or paste your text
4. Tap "Translate" or use the translate button
5. View the translation and use TTS to hear it aloud

#### 🎤 Hear Mode
1. Navigate to Hear from the home screen
2. Select target language
3. Tap the microphone button
4. Speak clearly into your device
5. The app will transcribe, translate, and speak the result

#### 📷 See Mode
1. Navigate to See from the home screen
2. Grant camera permission if prompted
3. Point your camera at text
4. Tap the capture button
5. View OCR results and translation instantly

---

## ⚙️ Configuration

### Translation Service Priority

The app uses a tiered approach for translation services:

1. **Google Cloud Translation API** (if `GOOGLE_TRANSLATE_API_KEY` is set)
   - Highest accuracy and quality
   - Requires API key and billing setup
   - Free tier: 500,000 characters/month

2. **LibreTranslate** (fallback)
   - Free, open-source translation service
   - No API key required
   - Public instance: `https://translate.cutie.dating`

3. **MyMemory** (final fallback)
   - Free translation API
   - Anonymous limit: 5,000 characters/day
   - Auto-detects source language when using "auto" mode

### Theme Configuration

The app supports three theme modes:
- **Light**: Light color scheme optimized for readability
- **Dark**: Dark theme with purple accent colors
- **System**: Follows device system theme

Change theme in **Settings → Appearance**.

---

## 🔒 Privacy & Security

- **On-Device OCR**: Camera translations use Google ML Kit, which processes images entirely on-device. No images are sent to external servers.
- **Secure Authentication**: User credentials are hashed using SHA-256 before storage
- **Local-First**: Translation history can be stored locally or optionally synced to Supabase
- **No Tracking**: The app does not include analytics or tracking SDKs
- **API Keys**: All sensitive keys are stored in `.env` (excluded from version control)

---

## 🧪 Testing

### Running Tests

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

### Manual Testing Checklist

- [ ] Create account and login
- [ ] Text translation with different language pairs
- [ ] Voice translation (microphone permission)
- [ ] Camera translation (camera permission)
- [ ] Theme switching (light/dark/system)
- [ ] Translation history saving and retrieval
- [ ] Language auto-detection
- [ ] Offline behavior (network errors)

---

## 📦 Building for Release

1. **Update version**
   - Edit `app/build.gradle.kts`
   - Update `versionCode` and `versionName`

2. **Generate signed APK**
   ```bash
   ./gradlew assembleRelease
   ```
   Output: `app/build/outputs/apk/release/app-release.apk`

3. **Generate App Bundle** (for Play Store)
   ```bash
   ./gradlew bundleRelease
   ```
   Output: `app/build/outputs/bundle/release/app-release.aab`

---

## 🛠️ Development

### Adding a New Language

1. Update language options in `HomeRoute.kt`:
   ```kotlin
   val sourceOptions = listOf("auto", "en", "es", "fr", "hi", "ur", "your-code")
   val targetOptions = listOf("en", "es", "fr", "hi", "ur", "your-code")
   ```

2. Add language label in `labelForCode()`:
   ```kotlin
   "your-code" -> "Your Language"
   ```

### Adding a New Translation Service

1. Add method to `TranslationService.kt`:
   ```kotlin
   private fun translateViaYourService(...): String {
       // Implementation
   }
   ```

2. Add to fallback chain in `translate()` method

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Format code with Android Studio's auto-formatter

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

### Contribution Guidelines

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/yourusername/echoon/issues)
- **Email**: support@echoon.app (if applicable)
- **Documentation**: See [PROJECT_PLAN.md](PROJECT_PLAN.md) for development roadmap

---

## 🙏 Acknowledgments

- **LibreTranslate** - Open-source translation service
- **MyMemory** - Free translation API
- **Google ML Kit** - On-device text recognition
- **Supabase** - Backend infrastructure
- **Jetpack Compose** - Modern Android UI toolkit
- **Material Design 3** - Design system

---

## 📊 Project Status

- ✅ Phase 1: Setup & basic app structure
- ✅ Phase 2: Language picker & text translation
- ✅ Phase 3: Voice translation
- ✅ Phase 4: Camera translation
- ✅ Phase 5: Supabase & authentication
- 🔄 Phase 6: Ads & polish (in progress)

---

<div align="center">

**Made with ❤️ using Kotlin and Jetpack Compose**

[⬆ Back to Top](#echoon)

</div>
