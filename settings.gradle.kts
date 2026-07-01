// Root Gradle settings file. Missing from the Google AI Studio export (GH issue #1) —
// AI Studio's cloud build supplies its own project topology and never surfaces this
// file to the downloaded zip. Reconstructed here so `./gradlew` has a project to build.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // FAIL_ON_PROJECT_REPOS keeps all repository declarations centralized here
    // instead of scattered across module build files, which is the current
    // Android Gradle Plugin recommendation and avoids silent repo drift.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "unlock-kindness"
include(":app")
