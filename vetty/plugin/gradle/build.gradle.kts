plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.jetbrains.kotlin.jvm)
}

gradlePlugin {
    plugins {
        create("vetty") {
            id = "app.thamani.vetty"
            implementationClass = "app.thamani.vetty.plugin.VettyPlugin"
            displayName = "Vetty"
            description = "JSON schema validation for network responses"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    // Gradle API is provided automatically by java-gradle-plugin
    compileOnly(gradleApi())
    compileOnly(libs.symbol.processing.gradle)
}
