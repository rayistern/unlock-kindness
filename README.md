# Unlock Kindness

An Android app that triggers a microdonation to [Merkos 302](https://merkos302.com) every time you unlock your phone.

- **Package:** `com.aistudio.pennydrop.xkqzwm`
- **Min Android:** 7.0 (API 24) — **Target:** Android 16 (API 36)
- **Stack:** Kotlin, Jetpack Compose, Room, Retrofit, Gemini API

## Status

This repo is the **Google AI Studio export** of the app — it contains only the `app/` module. The root Gradle files (`settings.gradle.kts`, `gradle/libs.versions.toml`, `gradlew`, etc.) are **not** included, so it will not build with `./gradlew` out of the box.

## How to install on your phone

You have three options, easiest first:

### 1. Re-open in Google AI Studio (easiest)

This zip was exported from AI Studio. Re-import it there and use the built-in "Install on device" / APK download flow. The `GEMINI_API_KEY` is injected automatically from your AI Studio Secrets panel.

### 2. Build in Android Studio

1. Install [Android Studio](https://developer.android.com/studio) (Hedgehog or newer).
2. Clone this repo and open the folder. Android Studio will prompt to **create the Gradle wrapper** and a default `settings.gradle.kts` — accept.
3. Create `gradle/libs.versions.toml` with version aliases for the dependencies referenced in `app/build.gradle.kts` (Compose BOM, Firebase BOM, Room, Retrofit, Moshi, Roborazzi, Secrets Gradle Plugin, KSP). Or copy one from a recent AI Studio export with the full project structure.
4. Copy `.env.example` to `.env` and replace `MY_GEMINI_API_KEY` with a real key from [aistudio.google.com](https://aistudio.google.com/app/apikey).
5. Connect your phone with **USB debugging on** ([instructions](https://developer.android.com/studio/debug/dev-options)) and click **Run**. Android Studio will build a debug APK, install it, and launch it.

### 3. Sideload a prebuilt APK

No prebuilt APK is published in this repo's [Releases](https://github.com/rayistern/unlock-kindness/releases) yet. Once one is, you can:

1. Download the `.apk` to your phone.
2. Enable **Install unknown apps** for your browser/file manager (Settings → Apps → Special access).
3. Tap the APK to install.

## Permissions

The app requests:

- `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` — to keep the unlock-tracking service alive in the background.
- `POST_NOTIFICATIONS` — to show the persistent service notification (required by Android 13+).

The unlock-tracking service has `foregroundServiceType="specialUse"` with subtype `"Unlock Tracking"` — note that Google Play requires explicit justification for this service type if you publish to the store.

## Configuration

| Key | Where | Purpose |
| --- | --- | --- |
| `GEMINI_API_KEY` | `.env` (not committed) | Server-side Gemini API calls. Get one at [aistudio.google.com/app/apikey](https://aistudio.google.com/app/apikey). |

The [Secrets Gradle Plugin](https://github.com/google/secrets-gradle-plugin) reads `.env` at build time and exposes the value via `BuildConfig`.

## License

Not yet specified.
