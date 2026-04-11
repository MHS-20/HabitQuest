plugins {
    id("spring-conventions")
}

description = "notification-service"

dependencies {
    implementation(libs.spring.starter)
    implementation(libs.spring.starter.mail)
    implementation(libs.spring.starter.json)

    testImplementation(libs.spring.kafka.test)
    testImplementation(libs.greenmail)
    testImplementation(libs.awaitility)
}