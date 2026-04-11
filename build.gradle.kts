plugins {}

group   = "habitquest"
version = "0.0.1-SNAPSHOT"

extra["testArchUnit"] = "1.1.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

tasks.register("checkAll") {
    dependsOn(subprojects
        .filter { it.path != ":services" }
        .map { "${it.path}:checkQuality" })
}
