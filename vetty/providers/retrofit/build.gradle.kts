plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":vetty:common:core"))
    implementation(libs.kotlin.stdlib)
    compileOnly(libs.okhttp.core)
    compileOnly(libs.retrofit.core)
}
