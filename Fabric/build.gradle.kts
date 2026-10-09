import com.hypherionmc.modpublisher.properties.CurseEnvironment
import com.hypherionmc.modpublisher.properties.ModLoader
import com.hypherionmc.modpublisher.properties.ReleaseType
import org.gradle.api.component.AdhocComponentWithVariants

plugins {
    id("com.gradleup.shadow")
    id("com.hypherionmc.modutils.modpublisher") version "2.2.3"
}

val minecraftVersion = project.properties["minecraft_version"] as String
val minecraftCompileVersion = project.properties["minecraft_compile_version"] as String
val minecraftGameVersions = (project.properties["minecraft_game_versions"] as String).split(",")

configurations {
    create("common")
    "common" {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    create("shadowBundle")
    compileClasspath.get().extendsFrom(configurations["common"])
    runtimeClasspath.get().extendsFrom(configurations["common"])
    "shadowBundle" {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

loom.accessWidenerPath.set(project(":Common").loom.accessWidenerPath)

loom.nestJars(tasks.shadowJar, configurations.named("include"))
loom.injectAccessWidener(tasks.shadowJar)

dependencies {
    implementation("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")
    api("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_api_version"]}+$minecraftCompileVersion")

    "common"(project(":Common")) { isTransitive = false }
    "shadowBundle"(project(":Common")) { isTransitive = false }

    implementation("com.electronwill.night-config:toml:${project.properties["nightconfig_version"]}")?.let { include(it) }
    implementation("com.electronwill.night-config:core:${project.properties["nightconfig_version"]}")?.let { include(it) }
    api("blue.endless:jankson:${project.properties["jankson_version"]}")?.let { include(it) }
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        exclude("architectury.common.json")
        configurations = listOf(project.configurations.getByName("shadowBundle"))
        archiveClassifier.set("")
    }

    jar {
        archiveClassifier.set("dev")
    }

    assemble {
        dependsOn(shadowJar)
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
    githubRepo.set("https://github.com/CorgiTaco-MC/CorgiLib")
    setReleaseType(ReleaseType.RELEASE)
    projectVersion.set("$minecraftVersion-${project.version}-Fabric")
    displayName.set("${project.properties["mod_name"]}-Fabric-$minecraftVersion-${project.version}")
    changelog.set(projectDir.toPath().parent.resolve("CHANGELOG.md").toFile().readText())
    artifact.set(tasks.shadowJar)
    gameVersions.set(minecraftGameVersions)
    setLoaders(ModLoader.FABRIC)
    setCurseEnvironment(CurseEnvironment.BOTH)
    setJavaVersions(JavaVersion.VERSION_25)
    val depends = mutableListOf("fabric-api")
    curseDepends.required.set(depends)
    modrinthDepends.required.set(depends)
}

private fun getPublishingCredentials(): Pair<String?, String?> {
    val curseForgeToken = (project.properties["curseforge_key"] ?: System.getenv("CURSEFORGE_KEY") ?: "") as String?
    val modrinthToken = (project.properties["modrinth_key"] ?: System.getenv("MODRINTH_KEY") ?: "") as String?
    return Pair(curseForgeToken, modrinthToken)
}

listOf("apiElements", "runtimeElements").forEach { configurationName ->
    configurations.named(configurationName) {
        outgoing.artifacts.clear()
        outgoing.artifact(tasks.shadowJar)
    }
}
(components["java"] as AdhocComponentWithVariants)
    .withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) { skip() }

tasks.named<Jar>("sourcesJar") {
    from(project(":Common").extensions.getByType<SourceSetContainer>()["main"].allSource)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
loom.mods.create("corgilib") {
    sourceSet(sourceSets.main.get())
    sourceSet(project(":Common").extensions.getByType<SourceSetContainer>()["main"])
}