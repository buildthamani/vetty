plugins {
    `java-gradle-plugin`
    `maven-publish`
    alias(libs.plugins.kotlin.jvm)
}

val vettyVersion = project.property("vetty.version") as String
val vettyGroup = project.property("vetty.group") as String

group = vettyGroup
version = vettyVersion

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

// Generate a properties file so the plugin knows its own version at runtime
val generateVersionFile = tasks.register("generateVettyVersionFile") {
    val outputDir = layout.buildDirectory.dir("generated/vetty-resources")
    val versionValue = vettyVersion
    val groupValue = vettyGroup
    outputs.dir(outputDir)
    doLast {
        val propsFile = outputDir.get().file("vetty-plugin.properties").asFile
        propsFile.parentFile.mkdirs()
        propsFile.writeText(
            """
            |version=$versionValue
            |group=$groupValue
            """.trimMargin()
        )
    }
}

sourceSets.main {
    resources.srcDir(generateVersionFile.map { it.outputs.files.singleFile })
}

tasks.named("processResources") {
    dependsOn(generateVersionFile)
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(gradleApi())
    compileOnly(libs.symbol.processing.gradle)
}

// Set artifact ID on the auto-generated pluginMaven publication
afterEvaluate {
    extensions.configure<PublishingExtension> {
        publications.withType<MavenPublication>().configureEach {
            if (name == "pluginMaven") {
                artifactId = "vetty-gradle-plugin"
            }
        }
    }
}
