plugins {
    id("org.springframework.boot")
}

description = "avatar-service"
val testArchUnit: String by rootProject.extra

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.hateoas:spring-hateoas")

    testImplementation("com.tngtech.archunit:archunit-junit5:$testArchUnit")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}