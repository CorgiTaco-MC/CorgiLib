import com.hypherionmc.modpublisher.properties.CurseEnvironment
import com.hypherionmc.modpublisher.properties.ModLoader
import com.hypherionmc.modpublisher.properties.ReleaseType
import dev.architectury.loom.accesstransformer.Aw2At
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

loom {
    accessWidenerPath.set(project(":Common").loom.accessWidenerPath)

    forge {
        convertAccessWideners.set(true)
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)

        mixinConfig("corgilib-common.mixins.json")
    }
}

loom.nestJars(tasks.shadowJar, configurations.named("include"))

dependencies {
    forge("net.minecraftforge:forge:$minecraftCompileVersion-${project.properties["forge_version"]}")
    "common"(project(":Common")) { isTransitive = false }
    "shadowBundle"(project(":Common")) { isTransitive = false }

    api("blue.endless:jankson:${project.properties["jankson_version"]}")?.let { include(it) }
}

Aw2At.addToTask(project, tasks.shadowJar) {
    accessWideners.add("corgilib.accesswidener")
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        exclude("architectury.common.json")
        configurations = listOf(project.configurations.getByName("shadowBundle"))
        archiveClassifier.set("")
        manifest.attributes("MixinConfigs" to "corgilib-common.mixins.json")
    }

    jar {
        archiveClassifier.set("dev")
        from(project(":Common").file("src/main/resources/corgilib.accesswidener"))
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
    projectVersion.set("$minecraftVersion-${project.version}-Forge")
    displayName.set("${project.properties["mod_name"]}-Forge-$minecraftVersion-${project.version}")
    changelog.set(projectDir.toPath().parent.resolve("CHANGELOG.md").toFile().readText())
    artifact.set(tasks.shadowJar)
    gameVersions.set(minecraftGameVersions)
    setLoaders(ModLoader.FORGE)
    setCurseEnvironment(CurseEnvironment.BOTH)
    setJavaVersions(JavaVersion.VERSION_25)
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
loom.mods.named("main") {
    sourceSet(project(":Common").extensions.getByType<SourceSetContainer>()["main"])
}