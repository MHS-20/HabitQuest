plugins {
    id("org.springframework.boot")
}

description = "Edge Service for habitquest"

dependencies {
    // --- Spring Boot ---
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-mvc")
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")

    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // --- Test ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:testcontainers")

    //testImplementation("org.wiremock:wiremock:3.5.4")
    testImplementation("org.wiremock:wiremock-standalone:3.5.4")
    testImplementation("io.rest-assured:rest-assured:5.4.0")
}