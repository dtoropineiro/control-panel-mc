plugins {
    java
    // runServer: arranca un servidor Paper local con tu plugin ya cargado
    id("xyz.jpenilla.run-paper") version "3.1.0"
}

group = "com.dario"
version = "1.0.0-SNAPSHOT"

// Versión de Minecraft/Paper del proyecto. Para actualizar, cambia este valor
// (y la versión de Java de más abajo si la nueva versión lo requiere).
val mcVersion = "26.2"

// Carpeta plugins del servidor Paper en WSL. Se puede sobrescribir con -PpluginsDir=/otra/carpeta/plugins
val pluginsDir: String = providers.gradleProperty("pluginsDir").getOrElse(
    if (System.getProperty("os.name").startsWith("Windows")) "//wsl.localhost/Ubuntu/home/dario/mc/server/plugins"
    else "/home/dario/mc/server/plugins"
)

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
}

dependencies {
    // API de Paper: solo para compilar, el servidor ya la incluye
    compileOnly("io.papermc.paper:paper-api:$mcVersion.build.+")
}

java {
    // Gradle compila con este JDK y lo descarga si no lo tienes instalado
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }

    processResources {
        // Rellena ${version} y ${apiVersion} en plugin.yml
        val props = mapOf(
            "version" to project.version,
            "apiVersion" to mcVersion.split(".").take(2).joinToString("."),
        )
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    runServer {
        minecraftVersion(mcVersion)
    }

    register<Copy>("deployLocal") {
        group = "deployment"
        description = "Compila el plugin y lo copia a la carpeta plugins del servidor local."
        from(jar)
        into(pluginsDir)
        // Borra los jars de versiones anteriores para que el servidor nunca cargue dos copias.
        val staleJars = fileTree(pluginsDir) { include("${project.name}-*.jar") }
        doFirst { staleJars.files.forEach { it.delete() } }
    }
}
