package com.example.data

/**
 * Centralizes access to the Gemini API key so the placeholder-vs-missing-key
 * decision lives in exactly one place (GH issue #1, 2026-07-01 CI-build work).
 *
 * Background: `GEMINI_API_KEY` is read from `.env` at build time by the
 * Secrets Gradle Plugin (see `secrets { ... }` in app/build.gradle.kts) and
 * surfaced as `BuildConfig.MY_GEMINI_API_KEY`. If `.env` is absent — which is
 * the normal case for a fresh clone or a CI run before the repo secret is
 * configured — the plugin falls back to the placeholder value declared in
 * `.env.example` ("MY_GEMINI_API_KEY"), NOT a build failure. That's why the
 * app compiles fine with no key configured at all: the placeholder string
 * satisfies the Gradle plugin, but is obviously not a usable API key.
 *
 * Nothing in this codebase calls the Gemini API yet (there is no Retrofit
 * service, ViewModel, or Composable that reads `BuildConfig.MY_GEMINI_API_KEY`
 * as of this writing). This object exists so that whenever that Gemini
 * integration is added, callers get a clear, actionable runtime error instead
 * of silently sending "MY_GEMINI_API_KEY" as a bearer token to Google's API
 * and getting a confusing 400/401 back.
 */
object GeminiConfig {

    /** The literal placeholder the Secrets Gradle Plugin substitutes when
     * `.env` (locally) or the `GEMINI_API_KEY` repo secret (in CI) hasn't
     * been supplied. Kept in one place so the check below doesn't drift
     * from `.env.example`. */
    private const val PLACEHOLDER_KEY = "MY_GEMINI_API_KEY"

    /**
     * Returns the configured Gemini API key, or throws
     * [IllegalStateException] with instructions if only the placeholder is
     * present.
     *
     * Call this — not `BuildConfig.MY_GEMINI_API_KEY` directly — from any
     * future Gemini API call site, so a missing key fails loudly and early
     * rather than as an opaque HTTP error deep in a Retrofit call.
     */
    fun requireApiKey(): String {
        val key = BuildConfig.MY_GEMINI_API_KEY
        check(key.isNotBlank() && key != PLACEHOLDER_KEY) {
            "GEMINI_API_KEY is not configured. Local builds: copy .env.example " +
                "to .env and replace $PLACEHOLDER_KEY with a real key from " +
                "https://aistudio.google.com/app/apikey. CI builds: add a " +
                "GEMINI_API_KEY repository secret in GitHub Settings > " +
                "Secrets and variables > Actions."
        }
        return key
    }

    /** True if a real (non-placeholder) key is configured. Use this for
     * feature-gating Gemini-dependent UI without throwing. */
    fun isApiKeyConfigured(): Boolean =
        BuildConfig.MY_GEMINI_API_KEY.isNotBlank() && BuildConfig.MY_GEMINI_API_KEY != PLACEHOLDER_KEY
}
