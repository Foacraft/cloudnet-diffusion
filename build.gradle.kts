plugins {
    java
    id("eu.cloudnetservice.juppiter") version "0.4.0"
    id("net.kyori.blossom") version "1.3.1"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.freefair.lombok") version "8.4"
}

repositories {
    mavenCentral()
    maven("https://repo.cloudnetservice.eu/repository/snapshots/")
    maven("https://s01.oss.sonatype.org/content/repositories/releases/")
    maven("https://repository.derklaro.dev/snapshots/")
    maven("https://repository.derklaro.dev/releases/")
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net/")
}

dependencies {
    compileOnly("dev.derklaro.aerogel:aerogel:2.1.0")

    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-api:1.20-R0.2-SNAPSHOT")

    compileOnly("eu.cloudnetservice.cloudnet:node:4.0.0-RC9")
    compileOnly("eu.cloudnetservice.cloudnet:bridge:4.0.0-RC9")
    compileOnly("eu.cloudnetservice.cloudnet:platform-inject-api:4.0.0-RC9")

    annotationProcessor("org.projectlombok:lombok:1.18.30")
    annotationProcessor("eu.cloudnetservice.cloudnet:platform-inject-processor:4.0.0-RC9")

    compileOnly(fileTree("libs"))
}

tasks {
    shadowJar {
        archiveClassifier.set("")
    }
    build {
        dependsOn(shadowJar)
    }
}

blossom {
    replaceToken("@project_name@", project.name)
    replaceToken("@project_version@", project.version.toString())
}

moduleJson {
    author = "Score2"
    name = project.name
    group = project.group.toString()
    main = "${rootProject.group}.Diffusion"
}
