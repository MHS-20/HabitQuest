rootProject.name = "habitquest"

include(
    "tracking-service",
    "notification-service",
    "avatar-service",
    "marketplace-service",
    "quest-service",
    "guild-service",
    "edge-service"
)

plugins {
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.1.9"
}

gitHooks {
    preCommit {
        tasks("checkAll")
    }
    createHooks(true)
}