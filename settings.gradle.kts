pluginManagement {
    includeBuild("build-logic")

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
}

rootProject.name = "habitquest"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
    "common",
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