plugins {
    java
    id("eu.cloudnetservice.juppiter") version "0.4.0"
    id("net.kyori.blossom") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.freefair.lombok") version "8.4"
}

repositories {
    mavenCentral()
    maven("https://repo.cloudnetservice.eu/repository/snapshots/")
    maven("https://repository.derklaro.dev/snapshots/")
    maven("https://repository.derklaro.dev/releases/")
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    compileOnly("dev.derklaro.aerogel:aerogel:2.1.0")
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

moduleJson {
    author = "Score2"
    name = project.name
    group = project.group.toString()
    main = "${rootProject.group}.TempReverse"
}