plugins {
    `kotlin-dsl`
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
private val springBoot    = catalog.findVersion("spring-boot").get().requiredVersion
private val dependencyManagement = catalog.findVersion("dependency-management").get().requiredVersion
private val spotless     = catalog.findVersion("spotless").get().requiredVersion
private val spotbugs     = catalog.findVersion("spotbugs").get().requiredVersion

val uiCatalog = extensions.getByType<VersionCatalogsExtension>().named("uiLibs")
private val kotlinMultiplatform  = uiCatalog.findVersion("kotlin").get().requiredVersion
private val androidGradle        = uiCatalog.findVersion("agp").get().requiredVersion
private val composeMultiplatform = uiCatalog.findVersion("composeMultiplatform").get().requiredVersion
private val detekt               = uiCatalog.findVersion("detekt").get().requiredVersion

dependencies {
    // --- Spring / Services ---
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:$springBoot")
    implementation("io.spring.gradle:dependency-management-plugin:$dependencyManagement")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:$spotless")
    // implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:spotbugs")

    // --- KMP / UI ---
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinMultiplatform}")
    implementation("com.android.tools.build:gradle:${androidGradle}")
    implementation("org.jetbrains.compose:compose-gradle-plugin:${composeMultiplatform}")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${detekt}")

    implementation(kotlin("stdlib-jdk8"))
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}