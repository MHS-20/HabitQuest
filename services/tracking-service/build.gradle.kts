plugins {
    id("org.springframework.boot")
}

description = "tracking-service"
val testArchUnit: String by rootProject.extra

dependencies {

    // --- Spring Boot ---
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.hateoas:spring-hateoas")
    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka")

    // --- Utilities ---
    implementation("org.springframework.retry:spring-retry")

    // --- Test ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:$testArchUnit")}