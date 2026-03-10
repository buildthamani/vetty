package app.thamani.vetty.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.Properties

class VettyPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("vetty", VettyExtension::class.java)

        // Apply KSP so the processor can run
        project.pluginManager.apply("com.google.devtools.ksp")

        // Resolve version: extension override > baked-in plugin version
        val pluginVersion = loadPluginVersion()

        project.afterEvaluate {
            val version = extension.version.getOrElse(pluginVersion)
            val group = GROUP

            project.dependencies.apply {
                // Annotations — compile + runtime
                add("implementation", "$group:vetty-annotations:$version")

                // KSP processor — compile-time only
                add("ksp", "$group:vetty-processor:$version")

                // Core runtime — validation engine, schema loader, data models
                add("implementation", "$group:vetty-core:$version")
            }
        }
    }

    private fun loadPluginVersion(): String {
        val props = Properties()
        val stream = javaClass.classLoader.getResourceAsStream("vetty-plugin.properties")
            ?: return FALLBACK_VERSION
        stream.use { props.load(it) }
        return props.getProperty("version", FALLBACK_VERSION)
    }

    companion object {
        private const val GROUP = "app.thamani.vetty"
        private const val FALLBACK_VERSION = "1.0.0"
    }
}
