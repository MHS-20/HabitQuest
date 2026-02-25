plugins {
    id("org.springframework.boot")
}

description = "notification-service"

dependencies {

    // --- Spring Boot ---
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // --- Utilities ---
    implementation("org.springframework.retry:spring-retry")

    // --- Runtime ---

    // --- Test ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}