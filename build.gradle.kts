// Root build file. Declares plugin versions once (via the version catalog) so
// every module `apply`-ing a plugin resolves the same version — `apply false`
// here means "make the plugin available on the classpath, don't apply it to
// the root project itself" (only app/build.gradle.kts applies it).
// Missing from the Google AI Studio export (GH issue #1); reconstructed.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.secrets) apply false
}
