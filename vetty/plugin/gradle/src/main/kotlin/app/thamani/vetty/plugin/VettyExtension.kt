package app.thamani.vetty.plugin

import org.gradle.api.provider.Property

abstract class VettyExtension {

    /** Override the Vetty artifact version (defaults to the plugin version). */
    abstract val version: Property<String>
}
