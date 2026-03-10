plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    // In a real project the Vetty plugin applies KSP automatically:
    //   id("app.thamani.vetty") version "<version>"
    // For local development we apply KSP and the project deps directly.
    alias(libs.plugins.google.devtools.ksp)
}

android {
    namespace = "app.vetty.retrofit"
    compileSdk {
        version =
            release(36) {
                minorApiLevel = 1
            }
    }

    defaultConfig {
        applicationId = "app.vetty.retrofit"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ── Vetty ──────────────────────────────────────────────────
    // These three are added automatically by the Vetty Gradle plugin.
    // In a published project you would only write:
    //   id("app.thamani.vetty") version "<version>"
    // and optionally add UI / provider modules below.
    implementation(project(":vetty:plugin:annotations"))
    ksp(project(":vetty:plugin:processor"))
    implementation(project(":vetty:common:core"))

    // Optional: Retrofit interceptor
    implementation(project(":vetty:providers:retrofit"))

    // Optional: Debug UI to view schema diffs
    implementation(project(":vetty:ui:coupled"))

    // ── Networking ─────────────────────────────────────────────
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.kotlinx.serialization.json)
}
