pluginManagement.repositories {
    maven("https://maven.fabricmc.net/")
    maven("https://maven.architectury.dev/")
    maven("https://maven.minecraftforge.net/")
    maven("https://maven.neoforged.net/releases/")
    maven("https://maven.firstdarkdev.xyz/releases")
    gradlePluginPortal()
}

plugins {
    id("com.gradle.develocity") version("4.0.2")
    id("org.gradle.toolchains.foojay-resolver-convention") version("1.0.0")
}

develocity.buildScan {
    termsOfUseUrl = "https://gradle.com/terms-of-service"
    termsOfUseAgree = "yes"
}

include("Common", "Fabric", "Forge")

rootProject.name = "CorgiLib"