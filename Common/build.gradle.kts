architectury {
    common("forge", "fabric")
    platformSetupLoomIde()
}

val minecraftVersion = project.properties["minecraft_version"] as String

loom.accessWidenerPath.set(file("src/main/resources/corgilib.accesswidener"))

sourceSets.main.get().resources.srcDir("src/main/generated/resources")

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")

    compileOnly("com.electronwill.night-config:toml:${project.properties["nightconfig_version"]}")
    compileOnly("blue.endless:jankson:${project.properties["jankson_version"]}")
}
