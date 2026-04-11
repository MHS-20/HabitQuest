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
    val appProjects = subprojects.filter { it.path != ":services" }
    dependsOn(appProjects.map { "${it.path}:checkstyleMain" })
    dependsOn(appProjects.map { "${it.path}:checkstyleTest" })
    dependsOn(appProjects.map { "${it.path}:pmdMain" })
    dependsOn(appProjects.map { "${it.path}:pmdTest" })
    dependsOn(appProjects.map { "${it.path}:spotlessCheck" })
    // dependsOn(appProjects.map { "${it.path}:spotbugsMain" })
    dependsOn(appProjects.map { "${it.path}:test" })
}