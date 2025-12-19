plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("io.ktor.plugin") version "3.3.3"
}

group = "com.example"
version = "1.0.0"

ktor {
    @OptIn(io.ktor.plugin.OpenApiPreview::class)
    openApi {
        title = "Bug Reproduction API"
        version = "1.0"
        summary = "Multi-module example reproducing Kotlin daemon crash and StackOverflowError"
        target = project.layout.buildDirectory.file("openapi.json")
    }
}

dependencies {
    // Dependency on controllers which transitively brings in interfaces
    // This cross-module dependency chain may trigger the bug
    implementation(project(":controllers"))

    implementation("io.ktor:ktor-server-core:3.3.3")
    implementation("io.ktor:ktor-server-netty:3.3.3")
    implementation("io.ktor:ktor-server-content-negotiation:3.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
