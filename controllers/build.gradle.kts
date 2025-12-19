plugins {
    kotlin("jvm")
}

group = "com.example"
version = "1.0.0"

dependencies {
    // Use api to expose interfaces transitively
    api(project(":interfaces"))
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
