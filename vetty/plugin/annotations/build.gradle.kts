plugins {
    id("java-library")
    `maven-publish`
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.kotlin.jvm)
}

group = project.property("vetty.group") as String
version = project.property("vetty.version") as String

publishing {
    publications {
        create<MavenPublication>("release") {
            artifactId = "vetty-annotations"
            from(components["java"])
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
}
