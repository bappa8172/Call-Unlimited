# Call Unlimited 📞

**Call Unlimited** is a modern, high-performance Android SIP (Session Initiation Protocol) calling application built with **Jetpack Compose** and the **Linphone SDK**. It is designed to deliver crystal-clear VoIP calls over the internet with "Studio Master" audio quality.

## ✨ Key Features

*   **Studio Master Quality Audio 🎧**:
    *   Powered by the **Opus** codec forced to **256kbps** bitrate (effectively lossless for voice).
    *   **Echo Cancellation** and **Automatic Gain Control (AGC)** enabled for superior clarity.
    *   **Adaptive Rate Control** with unlimited bandwidth ceiling to maintain stability without sacrificing quality.
    *   Optimized Network QoS (DSCP 46) for packet prioritization.
*   **Modern UI 🎨**: Built entirely with **Jetpack Compose** for a smooth, responsive, and declarative user interface.
*   **Secure Architecture 🔒**: Sensitive credentials are managed securely via local properties and not hardcoded in the source.
*   **Essential Call Controls**:
    *   Smart Dialpad.
    *   Call History/Logs.
    *   Mute & Speakerphone toggles.
    *   Background service support (Android 14 compatible).

## 🛠️ Tech Stack

*   **Language**: Kotlin
*   **UI**: Jetpack Compose (Material 3)
*   **VoIP Engine**: [Linphone SDK](https://github.com/BelledonneCommunications/linphone-sdk-android) (v5.3.0)
*   **Dependency Injection**: Dagger Hilt
*   **Networking**: Retrofit 2 & OkHttp
*   **Asynchronous**: Kotlin Coroutines & StateFlow
*   **Configuration**: Firebase Remote Config

## 🚀 Getting Started

### Prerequisites
*   Android Studio Iguana or newer.
*   JDK 17.

### Build Configuration
This project keeps sensitive URLs out of version control using a `secrets.properties` file.

1.  Clone the repository.
2.  Create a file named `secrets.properties` in the root directory of the project.
3.  Add the following line to `secrets.properties`:
    ```properties
    CREDENTIAL_URL=https://raw.githubusercontent.com/your-username/your-repo/main/creds.json
    ```
    *(Replace with your actual credential endpoint)*
4.  Sync Project with Gradle Files.
5.  Build and Run.

## 📱 Permissions

The app requests the minimum necessary permissions for VoIP:
*   `RECORD_AUDIO`: For capturing voice.
*   `USE_SIP`: For SIP signaling (if applicable).
*   `FOREGROUND_SERVICE`: To keep calls alive in the background.
*   *Note: Camera and Location permissions are explicitly removed for privacy.*

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

## 📄 License

This project is licensed under the [MIT License](LICENSE).
