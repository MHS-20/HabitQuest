import io.gitlab.arturbosch.detekt.Detekt

plugins {
    id("com.diffplug.spotless")
    id("io.gitlab.arturbosch.detekt")
}

val uiCatalog = extensions.getByType<VersionCatalogsExtension>().named("uiLibs")
private val detektVersion = uiCatalog.findVersion("detekt").get().requiredVersion

spotless {
    kotlin {
        target("src/**/*.kt")
        targetExclude("**/build/**", "**/generated/**")
        ktfmt().googleStyle()
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktfmt().googleStyle()
    }
}

detekt {
    toolVersion = detektVersion
    config.setFrom(file("${rootProject.projectDir}/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = false
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    dependsOn("spotlessApply")
}

tasks.register("checkQuality") {
    dependsOn("detekt", "spotlessCheck")
}