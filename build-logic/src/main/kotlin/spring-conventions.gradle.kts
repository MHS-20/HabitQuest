plugins {
    id("java-conventions")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

private val springCloudVersion   = "2025.0.0"
private val testcontainersVersion = "2.0.3"
private val micrometerVersion    = "1.16.4"

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        mavenBom("io.micrometer:micrometer-bom:$micrometerVersion")
    }
}

dependencies {
    // Observability
    "implementation"("io.micrometer:micrometer-tracing-bridge-otel")
    "implementation"("org.springframework.boot:spring-boot-starter-actuator")

    // Messaging
    "implementation"("org.springframework.cloud:spring-cloud-stream")
    "implementation"("org.springframework.cloud:spring-cloud-stream-binder-kafka")

    // Resilience
    "implementation"("io.github.resilience4j:resilience4j-spring-boot3")
    "implementation"("org.springframework.boot:spring-boot-starter-aop")

    // Metrics export
    "runtimeOnly"("io.micrometer:micrometer-registry-prometheus")
    "runtimeOnly"("io.opentelemetry:opentelemetry-exporter-otlp")
    "runtimeOnly"("io.github.resilience4j:resilience4j-micrometer")

    // Test dependencies
    "testImplementation"("org.springframework.boot:spring-boot-starter-test")
    "testImplementation"("org.springframework.boot:spring-boot-testcontainers")
    "testImplementation"("org.springframework.cloud:spring-cloud-stream-test-binder")
    "testImplementation"("org.springframework.kafka:spring-kafka-test")
    "testImplementation"("org.testcontainers:junit-jupiter")
    "testImplementation"("org.testcontainers:kafka")
}

tasks.named<Jar>("jar") {
    enabled = false
}