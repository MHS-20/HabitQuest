plugins {
    id("spring-conventions")
}

description = "avatar-service"

dependencies {
    implementation(libs.spring.starter.web)
    implementation(libs.spring.starter.valid)
    implementation(libs.spring.hateoas)

    testImplementation(libs.archunit)
    testRuntimeOnly(libs.junit.launcher)
}