plugins {
    id("org.springframework.boot")
}

description = "avatar-service"

dependencies {

    // --- Spring Boot ---
    implementation("org.springframework.boot:spring-boot-starter-kafka")
    implementation("org.springframework.boot:spring-boot-starter-session-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")

    // --- Runtime ---
    runtimeOnly("org.postgresql:postgresql")

    // --- Test ---
    testImplementation("org.springframework.boot:spring-boot-starter-kafka-test")
    testImplementation("org.springframework.boot:spring-boot-starter-session-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.2.1")

    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}