plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
}

group = project.property("vetty.group") as String
version = project.property("vetty.version") as String

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                artifactId = "vetty-overlay"
                from(components["release"])
            }
        }
    }
}

android {
    namespace = "dev.vetty.ui.overlay"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    buildFeatures {
        compose = true
    }

    publishing {
        singleVariant("release")
    }
}

dependencies {
    // api so consumers get VettyPanel, VettyViewModel, theme etc. transitively
    api(project(":vetty:common:presentation"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.coroutines.android)
    val bom = platform(libs.androidx.compose.bom)
    implementation(bom)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}
