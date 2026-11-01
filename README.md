# STEENIE BROWSER 2.0

A lightweight Android browser with a midnight blue/violet identity and a Brave-inspired browser layout. It uses Android System WebView (Chromium-based), not a separately bundled Chromium engine.

## Features
- Brave-inspired dark top bar, address/search field, shields control, tab counter and bottom navigation.
- Multiple tabs and a tab switcher.
- Private tabs with local-history suppression, no-cache mode and cookies disabled while a private tab is active.
- Gmail shortcut that opens Gmail in a browser tab; Google sign-in is handled by Google, and the app does not store passwords.
- Start page with shortcuts for Google, Gmail, YouTube, GitHub, Wikipedia, Facebook, Instagram and Reddit.
- Four built-in start-page background themes.
- Bookmarks, browsing history, downloads and desktop-site mode.
- Small built-in tracker-domain blocklist.

## Build
GitHub Actions builds the debug APK on every push to `main`, or manually through Actions → Build STEENIE BROWSER APK → Run workflow. Download the `STEENIE-BROWSER-2.0-debug-apk` artifact from the completed run.

## Limitations and privacy notes
- This is a Brave-inspired interface, not a copy of Brave's proprietary branding or source code.
- Android System WebView supplies the Chromium-based rendering engine. A full custom Chromium build is not practical on a low-memory ARMv7 phone.
- Private tabs reduce local traces but are not anonymous. Websites, network operators, DNS providers and internet providers may still observe activity. WebView cookie handling is process-wide on this Android API range, so cookies are disabled while a private tab is selected and re-enabled when a regular tab is selected; this is not a fully isolated multi-profile architecture.
- Tracker blocking is a small domain list, not Brave Shields or a complete ad blocker.
- Gmail is opened as a website shortcut. Google may restrict sign-in in embedded WebViews; if that happens, use the device's supported Chrome/browser sign-in flow.
