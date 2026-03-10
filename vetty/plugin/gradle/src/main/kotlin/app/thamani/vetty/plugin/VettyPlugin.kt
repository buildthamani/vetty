package app.thamani.vetty.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class VettyPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // 1. KSP must already be on the project — either applied by the user
        //    or we apply it ourselves if it isn't already.
        if (!project.plugins.hasPlugin("com.google.devtools.ksp")) {
            project.plugins.apply("com.google.devtools.ksp")
        }

        // 2. Wait until the project is evaluated so all configurations exist
        project.afterEvaluate {
            val version = "1.0.0" // could be read from a resource file or extension

            project.dependencies.apply {
                // Annotations — available at compile + runtime
                add("implementation", "app.thamani.vetty:vetty-annotations:$version")

                // Processor — handed to KSP only, never on the runtime classpath
                add("ksp", "app.thamani.vetty:vetty-plugin-processor:$version")

                // Core runtime
                add("implementation", "app.thamani.vetty:vetty-core:$version")
            }
        }
    }
}
