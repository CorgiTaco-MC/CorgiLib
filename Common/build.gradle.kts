val minecraftVersion = project.properties["minecraft_version"] as String

loom.accessWidenerPath.set(file("src/main/resources/corgilib.accesswidener"))

sourceSets.main.get().resources.srcDir("src/main/generated/resources")

dependencies {
    compileOnly("net.fabricmc:fabric-loader:${project.properties["fabric_loader_version"]}")

    compileOnlyApi("blue.endless:jankson:${project.properties["jankson_version"]}")
}
