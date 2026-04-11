plugins {
    id("spring-conventions")
}

description = "notification-service"

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-json")

    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.icegreen:greenmail:2.1.2")
    testImplementation("org.awaitility:awaitility")

}