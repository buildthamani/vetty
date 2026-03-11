plugins {
    alias(libs.plugins.kotlin.jvm)
    `maven-publish`
}

group = project.property("vetty.group") as String
version = project.property("vetty.version") as String

publishing {
    publications {
        create<MavenPublication>("release") {
            artifactId = "vetty-retrofit"
            from(components["java"])
        }
    }
}

dependencies {
    api(project(":vetty:common:core"))
    implementation(libs.kotlin.stdlib)
    compileOnly(libs.okhttp.core)
    compileOnly(libs.retrofit.core)
}
