plugins {
    java
    id("org.springframework.boot") version "4.0.3"
    id("io.spring.dependency-management") version "1.1.7"
    id("checkstyle")
    id("pmd")
    id("com.github.spotbugs") version "6.4.8"
    id("com.diffplug.spotless") version "8.2.1"
}

group = "habitquest"
version = "0.0.1-SNAPSHOT"
description = "notification-service"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val otelVersion = "2.23.0"
val springCloudVersion = "2025.0.0"

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // Spring Cloud
    // implementation("org.springframework.cloud:spring-cloud-stream-binder-rabbit")

    // Utilities
    implementation("org.springframework.retry:spring-retry")

    // Runtime
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("io.opentelemetry.javaagent:opentelemetry-javaagent:$otelVersion")

    // MacOS Apple Silicon
    // runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.101.Final:osx-aarch_64")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    // testImplementation("org.springframework.cloud:spring-cloud-stream")
    // testImplementation("org.springframework.cloud:spring-cloud-stream-test-binder")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

checkstyle {
    toolVersion = "13.2.0"
    configFile = file("${rootProject.projectDir}/config/checkstyle/checkstyle.xml")
}

pmd {
    toolVersion = "7.21.0"
}

spotbugs {
    toolVersion = "4.9.7"
}

spotless {
    java {
        target("src/**/*.java")
        targetExclude(
            "**/build/**",
            "**/generated/**"
        )

        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named("compileJava") {
    dependsOn("spotlessApply")
}

tasks.withType<Checkstyle>().configureEach {
    dependsOn("spotlessApply")
}