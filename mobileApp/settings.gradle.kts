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
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = "mapbox"
                password = "sk.eyJ1IjoibWVyaXMxMCIsImEiOiJjbWtscWZ3c2QwNmEwM2RwaG95MzZmNWEzIn0.UFoDzRc3LjBivYD83ksqmA"
            }
        }
    }
}


rootProject.name = "GetGo"
include(":app")
