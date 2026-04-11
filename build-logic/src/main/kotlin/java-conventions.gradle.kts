plugins {
    java
    id("com.diffplug.spotless")
    // id("com.github.spotbugs")
    checkstyle
    pmd
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}


spotless {
    java {
        target("src/**/*.java")
        targetExclude("**/build/**", "**/generated/**")
        googleJavaFormat()
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.named("compileJava") {
    dependsOn("spotlessApply")
}

checkstyle {
    toolVersion = "13.2.0"
    configFile = file("${rootProject.projectDir}/config/checkstyle/checkstyle.xml")
}

tasks.withType<Checkstyle>().configureEach {
    dependsOn("spotlessApply")
}


pmd {
    toolVersion = "7.21.0"
}

// configure<com.github.spotbugs.snom.SpotBugsExtension> {
//     toolVersion = "4.9.7"
// }

tasks.withType<Test> {
    useJUnitPlatform()
}