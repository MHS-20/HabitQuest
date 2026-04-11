plugins {
    id("spring-conventions")
}

description = "quest-service"

dependencies {
    implementation(libs.spring.starter.web)
    implementation(libs.spring.starter.valid)
    implementation(libs.spring.hateoas)

    testImplementation(libs.archunit)
}