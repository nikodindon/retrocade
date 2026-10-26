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
    ":games:api",
    ":games:tetris",
)
