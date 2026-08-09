# Chronosnap

Chronosnap is a lightweight, modern Android application designed to view and modify the EXIF date and time metadata of photos on your device in-place, without needing high-risk permissions.

## Tech Stack

- **UI Framework**: Jetpack Compose (Material 3)
- **Architecture**: MVVM with Kotlin Coroutines and StateFlow
- **Image Loading**: Coil for Jetpack Compose
- **Backdrop Blur**: [Cloudy](https://github.com/skydoves/cloudy) (frosted glass effects)
- **Metadata Engine**: `androidx.exifinterface:exifinterface`

## Build Instructions

### Prerequisites
- Android Studio Ladybug (or newer) or Android SDK Command-line Tools
- JDK 17 or higher

### Build via Command Line
To build the debug APK using Gradle, run:
```bash
./gradlew assembleDebug
```
The output APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

To run all unit tests:
```bash
./gradlew test
```

To clean the project:
```bash
./gradlew clean
```

## CI/CD Workflow

This repository includes a GitHub Actions CI/CD workflow configured under `.github/workflows/android.yml`. It automatically:
- Sets up JDK 17
- Sets up Gradle
- Builds the debug APK
- Uploads the build artifact

## License

This project is licensed under the [MIT License](LICENSE).

## Author

- **theonlyasdk**
