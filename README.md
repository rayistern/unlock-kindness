# Unlock Kindness

An Android app that triggers a microdonation to [Merkos 302](https://merkos302.com) every time you unlock your phone.

- **Package:** `com.aistudio.pennydrop.xkqzwm`
- **Min Android:** 7.0 (API 24) — **Target:** Android 16 (API 36)
- **Stack:** Kotlin, Jetpack Compose, Room, Retrofit, Gemini API

## Status

This repo started as a **Google AI Studio export** of the app, which originally contained only the `app/` module — the root Gradle scaffolding (`settings.gradle.kts`, `gradle/libs.versions.toml`, `gradlew`, etc.) and a few `res/xml` files are supplied by AI Studio's cloud build and were missing from the downloaded zip. As of [GH issue #1](https://github.com/rayistern/unlock-kindness/issues/1) (2026-07-01), that scaffolding has been reconstructed and committed, and a GitHub Actions workflow (`.github/workflows/build-apk.yml`) builds a debug APK on every push/PR — no AI Studio round-trip required.

## How to install on your phone

### 1. Download the latest CI build (easiest)

1. Go to this repo's [Actions tab](https://github.com/rayistern/unlock-kindness/actions/workflows/build-apk.yml) and open the most recent successful **Build debug APK** run on `main`.
2. Download the `app-debug` artifact from the run summary page and unzip it to get `app-debug.apk`.
3. Copy the APK to your phone, enable **Install unknown apps** for your browser/file manager (Settings → Apps → Special access), and tap the APK to install.

Note: this is a **debug** build (signed with the shared Android debug key, not a release key) — fine for personal sideloading, not for Play Store distribution.

### 2. Build in Android Studio

1. Install [Android Studio](https://developer.android.com/studio) (Ladybug or newer) with Android SDK 36 and JDK 21.
2. Clone this repo and open the folder — the Gradle wrapper and `settings.gradle.kts` are already committed, so no manual scaffolding is needed.
3. Copy `.env.example` to `.env` and replace `MY_GEMINI_API_KEY` with a real key from [aistudio.google.com](https://aistudio.google.com/app/apikey). (Optional — the app builds and runs fine without a real key; Gemini-dependent features will raise a clear runtime error if exercised without one. See `app/src/main/java/com/example/data/GeminiConfig.kt`.)
4. Connect your phone with **USB debugging on** ([instructions](https://developer.android.com/studio/debug/dev-options)) and click **Run**. Android Studio will build a debug APK, install it, and launch it.

### 3. Build from the command line

```bash
git clone https://github.com/rayistern/unlock-kindness.git
cd unlock-kindness
cp .env.example .env   # optional: edit in a real GEMINI_API_KEY
./gradlew assembleDebug
# APK lands at app/build/outputs/apk/debug/app-debug.apk
```

Requires JDK 17+ (CI uses JDK 21 Temurin) and network access to the Google/Maven Central repositories on first run (to download the Gradle distribution and dependencies).

## Permissions

The app requests:

- `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` — to keep the unlock-tracking service alive in the background.
- `POST_NOTIFICATIONS` — to show the persistent service notification (required by Android 13+).

The unlock-tracking service has `foregroundServiceType="specialUse"` with subtype `"Unlock Tracking"` — note that Google Play requires explicit justification for this service type if you publish to the store.

## Configuration

| Key | Where | Purpose |
| --- | --- | --- |
| `GEMINI_API_KEY` | `.env` (local, not committed) or the `GEMINI_API_KEY` **repository secret** (CI) | Server-side Gemini API calls. Get one at [aistudio.google.com/app/apikey](https://aistudio.google.com/app/apikey). |

The [Secrets Gradle Plugin](https://github.com/google/secrets-gradle-plugin) reads `.env` at build time and exposes the value via `BuildConfig`. **The build does not require a real key** — if `.env` is absent (local) or the `GEMINI_API_KEY` secret isn't configured (CI), the plugin falls back to the placeholder in `.env.example` and `assembleDebug` still succeeds. Code that actually calls the Gemini API should go through `GeminiConfig.requireApiKey()` (`app/src/main/java/com/example/data/GeminiConfig.kt`), which throws a clear, actionable error at the call site if only the placeholder is present, instead of failing obscurely inside a Retrofit call.

To enable real Gemini calls in CI: repo **Settings → Secrets and variables → Actions → New repository secret**, name `GEMINI_API_KEY`, value = a real key from the link above.

## License

Not yet specified.
