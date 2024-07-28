import com.hypherionmc.modpublisher.properties.CurseEnvironment
import com.hypherionmc.modpublisher.properties.ModLoader
import com.hypherionmc.modpublisher.properties.ReleaseType

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.hypherionmc.modutils.modpublisher") version "2.+"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val minecraftVersion = project.properties["minecraft_version"] as String
val jarName = base.archivesName.get() + "-fabric-$minecraftVersion"


configurations {
    create("common")
    create("shadowCommon")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    getByName("developmentFabric").extendsFrom(configurations["common"])
}

loom.accessWidenerPath.set(project(":Common").loom.accessWidenerPath)

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")
    modApi("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_api_version"]}+$minecraftVersion")

    "common"(project(":Common", "namedElements")) { isTransitive = false }
    "shadowCommon"(project(":Common", "transformProductionFabric")) { isTransitive = false }

    implementation("com.electronwill.night-config:toml:${project.properties["nightconfig_version"]}")?.let { include(it) }
    implementation("com.electronwill.night-config:core:${project.properties["nightconfig_version"]}")?.let { include(it) }
    "shadowCommon"("blue.endless:jankson:${project.properties["jankson_version"]}")

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

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        exclude("architectury.common.json")
        configurations = listOf(project.configurations.getByName("shadowCommon"))
        archiveClassifier.set("dev-shadow")
        relocate("blue.endless.jankson", "${project.group}.shadow.blue.endless.jankson")
        relocate("io.github.spair:imgui-java-binding:${project.properties["imgui_version"]}", "${project.group}.relocated.imgui-java-binding")
        relocate("io.github.spair:imgui-java-lwjgl3:${project.properties["imgui_version"]}", "${project.group}.relocated.imgui-java-lwjgl3")
        relocate("io.github.spair:imgui-java-natives-linux:${project.properties["imgui_version"]}", "${project.group}.relocated.imgui-java-natives-linux")
        relocate("io.github.spair:imgui-java-natives-windows:${project.properties["imgui_version"]}", "${project.group}.relocated.imgui-java-natives-windows")
        relocate("io.github.spair:imgui-java-natives-macos:${project.properties["imgui_version"]}", "${project.group}.relocated.imgui-java-natives-macos")
    }

    remapJar {
        injectAccessWidener.set(true)
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
    projectVersion.set("$minecraftVersion-${project.version}-fabric")
    displayName.set(jarName)
    changelog.set(projectDir.toPath().parent.resolve("CHANGELOG.md").toFile().readText())
    artifact.set(tasks.remapJar)
    setGameVersions(minecraftVersion)
    setLoaders(ModLoader.FABRIC, ModLoader.QUILT)
    setCurseEnvironment(CurseEnvironment.SERVER)
    setJavaVersions(JavaVersion.VERSION_17, JavaVersion.VERSION_18, JavaVersion.VERSION_19, JavaVersion.VERSION_20, JavaVersion.VERSION_21)
    val depends = mutableListOf("fabric-api")
    curseDepends.required.set(depends)
    modrinthDepends.required.set(depends)
}

publishing {
    publications.create<MavenPublication>("mavenFabric") {
        artifactId = "${project.properties["archives_base_name"]}" + "-fabric"
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