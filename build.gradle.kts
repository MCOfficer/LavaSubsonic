plugins {
    java
    alias(libs.plugins.lavalink)
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinSpring)
    alias(libs.plugins.shadow)
}

group = "me.mcofficer"
version = "0.1.0"

lavalinkPlugin {
    name = "lava-subsonic"
    apiVersion = libs.versions.lavalink.api
    serverVersion = libs.versions.lavalink.server
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    jar {
        enabled = false
    }

    shadowJar {
        archiveClassifier.set("")

        // Relocate subsonic-kotlin and its deps so they don't conflict
        relocate("dev.zt64.subsonic", "me.mcofficer.lavasubsonic.subsonic_shaded")
        relocate("kotlinx.serialization", "me.mcofficer.lavasubsonic.kotlinx_serialization_shaded")
        relocate("io.ktor", "me.mcofficer.lavasubsonic.ktor_shaded")

        // Exclude Lavalink's classes to avoid duplication
        exclude("org/jetbrains/annotations/**")

        mergeServiceFiles()
    }

    named("installPlugin") {
        dependsOn(shadowJar)
    }
}

dependencies {
    implementation(libs.subsonicClient)
    implementation(libs.ktorClient)
    implementation(libs.ktorEngine)
    implementation(libs.ktorSerialization)
    implementation(libs.kotlinCoroutines)
}


repositories {
    mavenCentral()
}