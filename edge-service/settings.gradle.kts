rootProject.name = "edge-service"

plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.1.9"
}

gitHooks {
    preCommit {
        tasks("build")
    }
    createHooks()
}
