rootProject.name = "oramacloud-client-kotlin"
include("client")

pluginManagement {
    dependencyResolutionManagement {
        repositories {
            mavenCentral()
            google()
            gradlePluginPortal()
        }
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}