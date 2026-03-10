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
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.hateoas:spring-hateoas")
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka")

    // --- Utilities ---
    implementation("org.springframework.retry:spring-retry")

    // --- Runtime ---
    runtimeOnly("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")
    runtimeOnly("org.springframework:spring-jdbc")

    // --- Test ---
    testImplementation("io.r2dbc:r2dbc-h2")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.github.dasniko:testcontainers-keycloak:${testKeycloakVersion}")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:r2dbc")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
    testImplementation("org.wiremock:wiremock-standalone")

}