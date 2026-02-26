import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.api.plugins.quality.Checkstyle
import org.gradle.jvm.tasks.Jar

plugins {
    id("org.springframework.boot") version "4.0.3" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("com.diffplug.spotless") version "8.2.1" apply false
    // id("com.github.spotbugs") version "6.4.8" apply false
    id("checkstyle")
    id("pmd")
    java
}

group = "habitquest"
version = "0.0.1-SNAPSHOT"

val otelVersion = "2.23.0"
val springCloudVersion = "2025.0.0"
val testcontainersVersion = "2.0.3"
val testKeycloakVersion by extra("4.1.1")

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    if (project.name == "services") return@subprojects
    apply(plugin = "java")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "com.diffplug.spotless")
    // apply(plugin = "com.github.spotbugs")
    apply(plugin = "checkstyle")
    apply(plugin = "pmd")

    group = rootProject.group
    version = rootProject.version

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
        imports {
            mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        }
    }

    dependencies {
        implementation("io.micrometer:micrometer-tracing-bridge-otel")
        implementation("org.springframework.boot:spring-boot-starter-actuator")

        runtimeOnly("io.micrometer:micrometer-registry-prometheus")
        runtimeOnly("io.opentelemetry:opentelemetry-exporter-otlp")
        runtimeOnly("io.opentelemetry.javaagent:opentelemetry-javaagent:$otelVersion")
        runtimeOnly("io.github.resilience4j:resilience4j-micrometer")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    checkstyle {
        toolVersion = "13.2.0"
        configFile = file("${rootProject.projectDir}/config/checkstyle/checkstyle.xml")
    }

    pmd {
        toolVersion = "7.21.0"
    }

    // configure<com.github.spotbugs.snom.SpotBugsExtension> {
    //     toolVersion = "4.9.7"
    // }

     configure<com.diffplug.gradle.spotless.SpotlessExtension> {
         java {
             target("src/**/*.java")
             targetExclude("**/build/**", "**/generated/**")
             googleJavaFormat()
             removeUnusedImports()
             trimTrailingWhitespace()
             endWithNewline()
         }
     }

    tasks.named("compileJava") {
        dependsOn("spotlessApply")
    }

    tasks.withType<Checkstyle>().configureEach {
        dependsOn("spotlessApply")
    }
}

tasks.register("checkAll") {
    val appProjects = subprojects.filter { it.path != ":services" }
    dependsOn(appProjects.map { "${it.path}:checkstyleMain" })
    dependsOn(appProjects.map { "${it.path}:pmdMain" })
     dependsOn(appProjects.map { "${it.path}:spotlessApply" })
    // dependsOn(appProjects.map { "${it.path}:spotbugsMain" })
    dependsOn(appProjects.map { "${it.path}:test" })
}

project(":services") {
    subprojects {
        apply(plugin = "org.springframework.boot")
        tasks.named<Jar>("jar") {
            enabled = false
        }
    }
}