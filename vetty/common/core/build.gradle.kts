plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

group = project.property("vetty.group") as String
version = project.property("vetty.version") as String

publishing {
    publications {
        create<MavenPublication>("release") {
            artifactId = "vetty-core"
            from(components["java"])
        }
    }
}

dependencies {
    api(project(":vetty:plugin:annotations"))

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coroutines.core)
}
