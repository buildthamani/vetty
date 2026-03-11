pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Vetty"
include(":app")

// plugins
include(":vetty:plugin:annotations")
include(":vetty:plugin:processor")
include(":vetty:plugin:gradle")

// common
include(":vetty:common:core")
include(":vetty:common:presentation")

// providers
include(":vetty:providers:retrofit")

// ui
include(":vetty:ui:overlay")

// samples
include(":sample:retrofit")
