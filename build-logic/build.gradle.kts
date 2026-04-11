plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:3.5.11")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:8.3.0")
    // implementation("com.github.spotbugs.snom:spotbugs-gradle-plugin:6.4.8")
    implementation(kotlin("stdlib-jdk8"))
}
repositories {
    mavenCentral()
}