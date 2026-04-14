plugins {
    id("java-conventions")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
private val springCloudVersion    = catalog.findVersion("spring-cloud").get().requiredVersion
private val testcontainersVersion = catalog.findVersion("testcontainers").get().requiredVersion
private val micrometerVersion     = catalog.findVersion("micrometer").get().requiredVersion

dependencyManagement {
    imports {
        mavenBom("org.testcontainers:testcontainers-bom:$testcontainersVersion")
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
        mavenBom("io.micrometer:micrometer-bom:$micrometerVersion")
    }
}

dependencies {
    implementation(project(":common"))

    // Observability
    implementation("io.micrometer:micrometer-tracing-bridge-otel")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Messaging
    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka")

    // Resilience
    implementation("io.github.resilience4j:resilience4j-spring-boot3")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // Metrics export
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("io.opentelemetry:opentelemetry-exporter-otlp")
    runtimeOnly("io.github.resilience4j:resilience4j-micrometer")

    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.cloud:spring-cloud-stream-test-binder")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:kafka")
}

tasks.named<Jar>("jar") {
    enabled = false
}