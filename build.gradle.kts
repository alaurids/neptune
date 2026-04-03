// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Root build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.ksp) apply false
    // DELETE THE LINE BELOW
    // alias(libs.plugins.kotlin.compose) apply false
}