pluginManagement {
    includeBuild("build-logic")

    val spotlessVersion = file("gradle/libs.versions.toml")
        .readLines()
        .first { it.startsWith("spotless") }
        .substringAfter("\"")
        .substringBefore("\"")

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins {
        id("com.diffplug.spotless") version spotlessVersion
    }
}

rootProject.name = "habitquest"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "services:tracking-service",
    "services:notification-service",
    "services:avatar-service",
    "services:marketplace-service",
    "services:quest-service",
    "services:guild-service",
    "services:edge-service",
    "habitquest-ui:composeApp"
)

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }

    versionCatalogs {
        create("uiLibs") {
            from(files("gradle/ui.versions.toml"))
        }
    }
}

plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.1.10"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

gitHooks {
    commitMsg { conventionalCommits() }
    preCommit {
        tasks("checkQuality")
    }
    createHooks(true)
}