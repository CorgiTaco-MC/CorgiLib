plugins {
    id("dev.architectury.loom-no-remap") version "1.17.493" apply false
    id("com.gradleup.shadow") version "9.6.1" apply false
    java
    idea
    `maven-publish`
}

val minecraftVersion = project.properties["minecraft_version"] as String
val minecraftCompileVersion = project.properties["minecraft_compile_version"] as String

allprojects {
    version = project.properties["version"] as String
    group = project.properties["group"] as String
}

subprojects {
    extensions.extraProperties["loom.platform"] = when (name) { "Forge" -> "forge"; "NeoForge" -> "neoforge"; else -> "fabric" }
    apply(plugin = "dev.architectury.loom-no-remap")
    apply(plugin = "maven-publish")

    base.archivesName.set(project.properties["archives_base_name"] as String + "-${project.name}-$minecraftVersion")

    repositories {
        mavenCentral()
        mavenLocal()

        maven("https://maven.fabricmc.net/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.architectury.dev/")
    }

    @Suppress("UnstableApiUsage")
    dependencies {
        "minecraft"("com.mojang:minecraft:$minecraftCompileVersion")
        compileOnly("org.jetbrains:annotations:26.0.2-1")
        compileOnly("com.google.auto.service:auto-service:1.1.1")
        annotationProcessor("com.google.auto.service:auto-service:1.1.1")
    }

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(25))
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(25)
    }

    publishing {
        publications.create<MavenPublication>("mavenJava") {
            artifactId = project.properties["archives_base_name"] as String + "-${project.name}"
            version = minecraftVersion + "-" + project.version.toString()
            from(components["java"])
        }

        repositories {
            mavenLocal()
            maven {
                val releasesRepoUrl = "https://maven.jt-dev.tech/releases"
                val snapshotsRepoUrl = "https://maven.jt-dev.tech/snapshots"
                url = uri(if (project.version.toString().endsWith("SNAPSHOT") || project.version.toString().startsWith("0")) snapshotsRepoUrl else releasesRepoUrl)
                name = "JTDev-Maven-Repository"
                credentials {
                    username = project.properties["repoLogin"]?.toString()
                    password = project.properties["repoPassword"]?.toString()
                }
            }
        }
    }
}