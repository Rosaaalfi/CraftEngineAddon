plugins {
    id("java-library")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "me.chyxelmc"
version = "1.0.0"

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenLocal()
}

dependencies {
    paperweight.paperDevBundle("1.21.1-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly(files("C:/Users/Aniko/IdeaProjects/CraftEngineAddon/libs/craft-engine-core-0.0.67.jar"))
    compileOnly(files("C:/Users/Aniko/IdeaProjects/CraftEngineAddon/libs/craft-engine-bukkit-0.0.67.jar"))
    compileOnly(files("C:/Users/Aniko/IdeaProjects/CraftEngineAddon/libs/craft-engine-paper-plugin-0.0.67.jar"))
}

paperweight {
    reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    processResources {
        filteringCharset = "UTF-8"
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filesMatching("paper-plugin.yml") {
            expand(props)
        }
    }

    runServer {
        minecraftVersion("1.21.1")
        jvmArgs("-Xms2G", "-Xmx2G")
    }
}
