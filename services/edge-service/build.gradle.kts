plugins {
    id("spring-conventions")
}

description = "Edge Service for habitquest"

dependencies {
    implementation(libs.spring.starter.json)
    implementation(libs.spring.starter.security)
    implementation(libs.spring.starter.webflux)
    implementation(libs.spring.cloud.circuitbreaker)
    implementation(libs.spring.cloud.gateway)
    implementation(libs.jjwt.api)

    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    testImplementation(libs.spring.security.test)
    testImplementation(libs.jakarta.servlet.api)
    testImplementation(libs.wiremock)
    testImplementation(libs.rest.assured)
}