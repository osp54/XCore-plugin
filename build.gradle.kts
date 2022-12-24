plugins {
    java
}

group = "org.xcore.plugin"
version = "1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://maven.xpdustry.fr/releases")
}

dependencies {
    val mindustryVersion = "140.4"
    compileOnly("com.github.Anuken.Arc:arc-core:v$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:core:v$mindustryVersion")
    compileOnly("com.github.Anuken.Mindustry:server:v$mindustryVersion")

    compileOnly("fr.xpdustry:javelin-mindustry:1.2.0")

    implementation("com.google.code.gson:gson:2.10")
    implementation("net.dv8tion:JDA:5.0.0-beta.2")

    implementation("org.jline:jline-reader:3.21.0")
    implementation("org.jline:jline-console:3.21.0")
}

tasks.jar {
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}