plugins {
    id("io.gitlab.arturbosch.detekt")
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jlleitschuh.gradle.ktlint")
}

val uiCatalog = extensions.getByType<VersionCatalogsExtension>().named("uiLibs")
val detektVersion = uiCatalog.findVersion("detekt").get().requiredVersion
val ktlintVersion = uiCatalog.findVersion("ktlint").get().requiredVersion

detekt {
    toolVersion = detektVersion
    config.setFrom(file("${rootProject.projectDir}/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = false
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    version.set(ktlintVersion)
    android.set(true)
    ignoreFailures.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
    }
}

tasks.register("checkQuality") {
    dependsOn("detekt", "ktlintFormat")
}

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