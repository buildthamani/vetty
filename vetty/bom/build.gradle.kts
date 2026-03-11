plugins {
    `java-platform`
    `maven-publish`
}

val vettyVersion = project.property("vetty.version") as String
val vettyGroup = project.property("vetty.group") as String

group = vettyGroup
version = vettyVersion

dependencies {
    constraints {
        api("$vettyGroup:vetty-annotations:$vettyVersion")
        api("$vettyGroup:vetty-processor:$vettyVersion")
        api("$vettyGroup:vetty-gradle-plugin:$vettyVersion")
        api("$vettyGroup:vetty-core:$vettyVersion")
        api("$vettyGroup:vetty-presentation:$vettyVersion")
        api("$vettyGroup:vetty-retrofit:$vettyVersion")
        api("$vettyGroup:vetty-overlay:$vettyVersion")
    }
}

publishing {
    publications {
        create<MavenPublication>("bom") {
            artifactId = "vetty-bom"
            from(components["javaPlatform"])
        }
    }
}
