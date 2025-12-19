plugins {
    kotlin("jvm") version "2.2.20" apply false
    kotlin("plugin.serialization") version "2.2.20" apply false
}

group = "com.example"
version = "1.0.0"

subprojects {
    repositories {
        mavenCentral()
    }
}
