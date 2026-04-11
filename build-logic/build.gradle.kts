plugins {
    `kotlin-dsl`
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
private val springBoot    = catalog.findVersion("spring-boot").get().requiredVersion
private val dependencyManagement = catalog.findVersion("dependency-management").get().requiredVersion
private val spotless     = catalog.findVersion("spotless").get().requiredVersion
private val spotbugs     = catalog.findVersion("spotbugs").get().requiredVersion

dependencies {
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:$springBoot")
    implementation("io.spring.gradle:dependency-management-plugin:$dependencyManagement")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:$spotless")
    // implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:spotbugs")
    implementation(kotlin("stdlib-jdk8"))
}

repositories {
    mavenCentral()
}