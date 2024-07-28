import com.hypherionmc.modpublisher.properties.CurseEnvironment
import com.hypherionmc.modpublisher.properties.ModLoader
import com.hypherionmc.modpublisher.properties.ReleaseType

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.hypherionmc.modutils.modpublisher") version "2.+"
}

architectury {
    platformSetupLoomIde()
    forge()
}

val minecraftVersion = project.properties["minecraft_version"] as String
val jarName = base.archivesName.get() + "-forge-$minecraftVersion"

configurations {
    create("common")
    create("shadowCommon")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentForge").extendsFrom(configurations["common"])
}

loom {
    accessWidenerPath.set(project(":Common").loom.accessWidenerPath)

    forge {
        convertAccessWideners.set(true)
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)

        mixinConfig("corgilib-common.mixins.json")
    }

    // Forge Datagen Gradle config.  Remove if not using Forge datagen
    runs.create("datagen") {
        data()
        programArgs("--all", "--mod", "corgilib")
        programArgs("--output", project(":Common").file("src/main/generated/resources").absolutePath)
        programArgs("--existing", project(":Common").file("src/main/resources").absolutePath)
    }
}

dependencies {
    if ((project.properties["use_neoforge"] as String).toBoolean())
        forge("net.neoforged:forge:$minecraftVersion-${project.properties["neoforge_version"]}")
    else forge("net.minecraftforge:forge:$minecraftVersion-${project.properties["forge_version"]}")

    "common"(project(":Common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":Common", "transformProductionForge")) { isTransitive = false }

    include("blue.endless:jankson:${project.properties["jankson_version"]}")

    "shadowCommon"("io.github.spair:imgui-java-binding:${project.properties["imgui_version"]}")
    "shadowCommon"("io.github.spair:imgui-java-lwjgl3:${project.properties["imgui_version"]}") {
        exclude(group = "org.lwjgl")
        exclude(group = "org.lwjgl.lwjgl")
    }

    "shadowCommon"("io.github.spair:imgui-java-natives-windows:${project.properties["imgui_version"]}")
    "shadowCommon"("io.github.spair:imgui-java-natives-linux:${project.properties["imgui_version"]}")
}

tasks {
    base.archivesName.set(jarName)
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        exclude("fabric.mod.json", "architectury.common.json")
        configurations = listOf(project.configurations.getByName("shadowCommon"))
        archiveClassifier.set("dev-shadow")
        relocate("io.github.spair:imgui-java-binding:${project.properties["imgui_version"]}", "${project.group}.relocated.imgui-java-binding")
        relocate("io.github.spair:imgui-java-lwjgl3:${project.properties["imgui_version"]}", "${project.group}.relocated.imgui-java-lwjgl3")
        relocate("io.github.spair:imgui-java-natives-linux:${project.properties["imgui_version"]}", "${project.group}.relocated.imgui-java-natives-linux")
        relocate("io.github.spair:imgui-java-natives-windows:${project.properties["imgui_version"]}", "${project.group}.relocated.imgui-java-natives-windows")
        relocate("io.github.spair:imgui-java-natives-macos:${project.properties["imgui_version"]}", "${project.group}.relocated.imgui-java-natives-macos")
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar)
    }

    jar.get().archiveClassifier.set("dev")

    sourcesJar {
        val commonSources = project(":Common").tasks.sourcesJar
        dependsOn(commonSources)
        from(commonSources.get().archiveFile.map { zipTree(it) })
    }
}

components {
    java.run {
        if (this is AdhocComponentWithVariants)
            withVariantsFromConfiguration(project.configurations.shadowRuntimeElements.get()) { skip() }
    }
}

publisher {
    apiKeys {
        curseforge(getPublishingCredentials().first)
        modrinth(getPublishingCredentials().second)
        github(project.properties["github_token"].toString())
    }

    curseID.set(project.properties["curseforge_id"].toString())
    modrinthID.set(project.properties["modrinth_id"].toString())
    githubRepo.set("https://github.com/CorgiTaco/Oh-The-Trees-Youll-Grow")
    setReleaseType(ReleaseType.BETA)
    projectVersion.set("$minecraftVersion-${project.version}-forge")
    displayName.set(jarName)
    changelog.set(projectDir.toPath().parent.resolve("CHANGELOG.md").toFile().readText())
    artifact.set(tasks.remapJar)
    setGameVersions(minecraftVersion)
    setLoaders(ModLoader.FORGE, ModLoader.NEOFORGE)
    setCurseEnvironment(CurseEnvironment.SERVER)
    setJavaVersions(JavaVersion.VERSION_17, JavaVersion.VERSION_18, JavaVersion.VERSION_19, JavaVersion.VERSION_20, JavaVersion.VERSION_21)
}


publishing {
    publications.create<MavenPublication>("mavenForge") {
        artifactId = "${project.properties["archives_base_name"]}" + "-forge"
        version = "$minecraftVersion-" + project.version.toString()
        from(components["java"])
    }

    repositories {
        mavenLocal()
        maven {
            val releasesRepoUrl = "https://maven.jt-dev.tech/releases"
            val snapshotsRepoUrl = "https://maven.jt-dev.tech/snapshots"
            url = uri(if (project.version.toString().endsWith("SNAPSHOT") || project.version.toString().startsWith("0")) snapshotsRepoUrl else releasesRepoUrl)
            name = "ExampleRepo"
            credentials {
                username = project.properties["repoLogin"]?.toString()
                password = project.properties["repoPassword"]?.toString()
            }
        }
    }
}

private fun getPublishingCredentials(): Pair<String?, String?> {
    val curseForgeToken = (project.findProperty("curseforge_key") ?: System.getenv("CURSEFORGE_KEY") ?: "") as String?
    val modrinthToken = (project.findProperty("modrinth_key") ?: System.getenv("MODRINTH_KEY") ?: "") as String?
    return Pair(curseForgeToken, modrinthToken)
}