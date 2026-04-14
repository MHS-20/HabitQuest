group   = "habitquest"
version = "0.0.1-SNAPSHOT"

tasks.register("checkQuality") {
    subprojects.forEach { sub ->
        sub.tasks.findByName("checkQuality")?.let { dependsOn(it) }
    }
}