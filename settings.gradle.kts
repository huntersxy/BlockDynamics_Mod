pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("dev.kikugie.stonecutter") version "0.9.7"
}

rootProject.name = "BlockDynamics_Mod"

stonecutter {
    create(rootProject) {
        // 1.21 起全部正式版（含全部补丁版本）；beta-only 的 NeoForge pin 见 build.neoforge.gradle.kts 注释
        listOf(
            "1.21", "1.21.1", "1.21.2", "1.21.3", "1.21.4", "1.21.5",
            "1.21.6", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11",
            "26.1", "26.1.1", "26.1.2", "26.2", "26.3",
        ).forEach { version ->
            version("$version-neoforge", version).buildscript = "build.neoforge.gradle.kts"
        }
        vcsVersion = "1.21.1-neoforge"
    }
}
