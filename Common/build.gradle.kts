architectury {
    common("forge", "fabric")
    platformSetupLoomIde()
}

val minecraftVersion = project.properties["minecraft_version"] as String

loom.accessWidenerPath.set(file("src/main/resources/corgilib.accesswidener"))

sourceSets.main.get().resources.srcDir("src/main/generated/resources")

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")

    compileOnly("com.electronwill.night-config:toml:${project.properties["nightconfig_version"]}")
    compileOnly("blue.endless:jankson:${project.properties["jankson_version"]}")

    compileOnly("io.github.spair:imgui-java-binding:${project.properties["imgui_version"]}")
    compileOnly("io.github.spair:imgui-java-lwjgl3:${project.properties["imgui_version"]}")

    compileOnly("io.github.spair:imgui-java-natives-windows:${project.properties["imgui_version"]}")
    compileOnly("io.github.spair:imgui-java-natives-linux:${project.properties["imgui_version"]}")
}

publishing {
    publications.create<MavenPublication>("mavenCommon") {
        artifactId = "${project.properties["archives_base_name"]}" + "-Common"
        from(components["java"])
    }

    repositories {
        mavenLocal()
        maven {
            val releasesRepoUrl = "https://example.com/releases"
            val snapshotsRepoUrl = "https://example.com/snapshots"
            url = uri(if (project.version.toString().endsWith("SNAPSHOT") || project.version.toString().startsWith("0")) snapshotsRepoUrl else releasesRepoUrl)
            name = "ExampleRepo"
            credentials {
                username = project.properties["repoLogin"]?.toString()
                password = project.properties["repoPassword"]?.toString()
            }
        }
    }
}