plugins {
    id("org.springframework.boot")
}

description = "guild-service"

val testKeycloakVersion: String by rootProject.extra

dependencies {

    // --- Spring Boot ---
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // --- Utilities ---
    implementation("org.springframework.retry:spring-retry")

    // --- Runtime ---
    runtimeOnly("org.flywaydb:flyway-core")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    runtimeOnly("org.springframework:spring-jdbc")

    // --- Test ---
    testImplementation("io.r2dbc:r2dbc-h2")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.github.dasniko:testcontainers-keycloak:${testKeycloakVersion}")
    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-r2dbc")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.2.1")
}