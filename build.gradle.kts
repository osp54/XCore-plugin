import fr.xpdustry.toxopid.Toxopid
import fr.xpdustry.toxopid.dsl.mindustryDependencies
import fr.xpdustry.toxopid.spec.ModMetadata
import fr.xpdustry.toxopid.spec.ModPlatform
import fr.xpdustry.toxopid.task.MindustryExec

plugins {
    java
    id("fr.xpdustry.toxopid") version "3.0.0"
}

group = "org.xcore.plugin"
version = "2.2"
val mindustryVersion = "142"

toxopid {
    compileVersion.set("v$mindustryVersion")
    runtimeVersion.set("v$mindustryVersion")
    platforms.add(ModPlatform.HEADLESS)
}

val metadata = ModMetadata(
    name = "xcore-plugin",
    displayName = "XCore-plugin",
    description = "The main plugin for XCore servers.",
    author = "osp54, OSPx#7122",
    version = project.version.toString(),
    minGameVersion = mindustryVersion,
    main = "${project.group}.XcorePlugin",
    dependencies = mutableListOf("xpdustry-javelin")
)

repositories {
    mavenCentral()
    maven(url = "https://www.jitpack.io")
    maven(url = "https://maven.xpdustry.fr/releases")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.26")

    implementation("org.json:json:20230227")

    mindustryDependencies()

    implementation("com.github.xzxADIxzx.useful-stuffs:server-menus:c48df39e17")
    compileOnly("fr.xpdustry:javelin-mindustry:1.2.0")

    implementation("org.mongodb:mongodb-driver-sync:4.9.0")
    implementation("com.google.code.gson:gson:2.10")

    implementation("com.discord4j:discord4j-core:3.2.2")

    implementation("org.jline:jline-reader:3.21.0")
    implementation("org.jline:jline-console:3.21.0")
}

tasks.jar {
    doFirst {
        val temp = temporaryDir.resolve("plugin.json")
        temp.writeText(metadata.toJson(true))
        from(temp)
    }

    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

// Required for the GitHub actions
tasks.register("getProjectVersion") {
    doLast { println(project.version.toString()) }
}

tasks.register("runMainServer", MindustryExec::class.java) {
    group = Toxopid.TASK_GROUP_NAME
    classpath(tasks.downloadMindustryServer)
    mainClass.convention("mindustry.server.ServerLauncher")
    modsPath.convention("./config/mods")
    standardInput = System.`in`
    mods.setFrom(setOf(tasks.jar, project.file("./build/libs/Javelin.jar")))
}

tasks.register("runServer", MindustryExec::class.java) {
    group = Toxopid.TASK_GROUP_NAME
    classpath(tasks.downloadMindustryServer)
    mainClass.convention("mindustry.server.ServerLauncher")
    modsPath.convention("./config/mods")
    standardInput = System.`in`
    mods.setFrom(setOf(tasks.jar, project.file("./build/libs/Javelin.jar")))
}
