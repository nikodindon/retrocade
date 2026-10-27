pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Retrocade"

include(
    ":app",
    ":core:common",
    ":core:engine",
    ":core:audio",
    ":core:ui",
    ":core:ads",
    ":core:billing",
    ":core:storage",
)

// Every module under games/ is picked up automatically, including the games/api
// contract module. Adding a game therefore only creates its module and adds an
// app dependency: no hub, navigation or settings list to edit.
file("games")
    .listFiles()
    ?.filter { it.isDirectory && it.resolve("build.gradle.kts").isFile }
    ?.sortedBy { it.name }
    ?.forEach { include(":games:${it.name}") }
