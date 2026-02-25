plugins {
    id("org.springframework.boot")
}

description = "tracking-service"

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
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.github.dasniko:testcontainers-keycloak:${rootProject.extra["testKeycloakVersion"]}")
}