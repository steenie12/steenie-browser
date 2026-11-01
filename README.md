# STEENIE BROWSER

A lightweight Android browser starter project with a midnight black / electric blue / violet UI.

## Engine note
This first build uses Android System WebView, which is Chromium-based and supplied/updated by the Android device's WebView provider (often Android System WebView or Chrome). It does **not** bundle the full standalone Chromium engine. Building and maintaining a custom Chromium engine is far beyond a practical ARMv7 Termux build.

## Features in this starter
- URL bar with web search fallback (Google)
- Back, forward, home, refresh via page reload behavior
- Multiple tabs and tab switcher
- Bookmarks and basic browsing history
- DownloadManager downloads
- Basic private-tab mode (local history is not saved while active)
- Small built-in tracker-domain blocklist (not a complete ad blocker)
- Desktop-mode toggle for newly created tabs
- Custom STEENIE icon and dark visual theme

## Build APK using GitHub Actions (recommended for Android 11 ARMv7 phone)
1. Create a GitHub repository named `steenie-browser`.
2. Upload/push all files in this project to the repository's `main` branch.
3. Open the repository on GitHub → **Actions** → **Build STEENIE BROWSER APK** → **Run workflow**.
4. Wait for the workflow to finish successfully.
5. Open the completed workflow run → **Artifacts** → download `STEENIE-BROWSER-debug-apk`.
6. Extract the ZIP on your phone and install `app-debug.apk`. If Android asks, allow installation from that file manager/browser.

## Build locally (Android Studio / Linux)
Requires JDK 17, Android SDK platform 35, Build Tools 35.0.0, and Gradle 8.9+.

```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Termux note
The source can be edited in Termux, but a full Android SDK/Gradle build is not recommended on a 1.8 GB ARMv7 phone. Use GitHub Actions as the remote build machine and download the APK to the phone.

## Important limitations
- Private mode is a basic local-history feature, not anonymity. Websites, networks, DNS providers, and internet providers may still observe activity.
- Tracker blocking uses a small hostname list and is not equivalent to Brave Shields or uBlock Origin.
- WebView capabilities and updates depend on the Android System WebView provider installed on the device.
- No extension ecosystem, sync, password manager, or full Chromium engine is included yet.
