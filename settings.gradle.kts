rootProject.name = "habitquest"

include(
    "services:tracking-service",
    "services:notification-service",
    "services:avatar-service",
    "services:marketplace-service",
    "services:quest-service",
    "services:guild-service",
    "services:edge-service"
)

plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.1.12"
}

gitHooks {
    commitMsg { conventionalCommits() }
    preCommit {
        tasks("checkAll")
    }

    createHooks(true)
}