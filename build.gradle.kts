plugins {
    id("com.diffplug.spotless") apply false
}

group   = "habitquest"
version = "0.0.1-SNAPSHOT"

allprojects {
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

tasks.register("checkAll") {
    dependsOn(subprojects
        .filter { it.path != ":services" && it.path != ":habitquest-ui" }
        .map { "${it.path}:checkQuality" })
}