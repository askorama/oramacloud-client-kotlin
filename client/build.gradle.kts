import org.jetbrains.kotlin.konan.target.HostManager

val GROUP: String by project
val VERSION_NAME: String by project

version = VERSION_NAME
println("Project version is set to: ${version}")



plugins {
    kotlin("multiplatform") version "1.9.0"
    kotlin("plugin.serialization") version "2.0.0" apply false
    id("com.vanniktech.maven.publish") version "0.29.0"
    alias(libs.plugins.jvm) apply false
    `java-library`
}

kotlin {
    jvm()

    if (HostManager.hostIsMac) {
        macosX64()
        macosArm64()
        iosX64()
        iosArm64()
        iosSimulatorArm64()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:3.0.0-beta-2")
                implementation("io.ktor:ktor-client-cio:3.0.0-beta-2")
                implementation("io.ktor:ktor-client-logging:3.0.0-beta-2")
                implementation("io.ktor:ktor-client-content-negotiation:3.0.0-beta-2")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0-beta-2")
                implementation("com.benasher44:uuid:0.8.4")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
            }
        }
    }
}

apply(plugin = "kotlinx-serialization")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

publishing {
    repositories {
        val nexusRepositoryUrl = System.getenv("NEXUS_REPOSITORY_URL")
        if (nexusRepositoryUrl != null) {
            maven {
                name = "Nexus"
                url = uri(nexusRepositoryUrl)
                isAllowInsecureProtocol = true
            }
        }
        mavenCentral()
    }
}