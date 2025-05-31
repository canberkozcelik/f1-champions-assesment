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

rootProject.name = "F1 Champions"

// Application module
include(":app")

// Core modules
include(":core")
include(":domain")
include(":data")

// Feature modules
include(":feature:seasonslist")
include(":feature:racewinners")

// Set project directories for feature modules
project(":feature:seasonslist").projectDir = file("feature/seasonslist")
project(":feature:racewinners").projectDir = file("feature/racewinners")
