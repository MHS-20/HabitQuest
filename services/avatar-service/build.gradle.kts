plugins {
    id("org.springframework.boot")
}

description = "avatar-service"

dependencies {

    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.session:spring-session-jdbc")
    implementation("org.springframework.hateoas:spring-hateoas")
    implementation("org.springframework.cloud:spring-cloud-stream")
    implementation("org.springframework.cloud:spring-cloud-stream-binder-kafka")

    // --- Runtime ---
    runtimeOnly("org.postgresql:postgresql")

    // --- Test ---
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")

    testRuntimeOnly("com.h2database:h2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}