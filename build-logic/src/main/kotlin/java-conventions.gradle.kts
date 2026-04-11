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

checkstyle {
    toolVersion = "13.2.0"
    configFile = file("${rootProject.projectDir}/config/checkstyle/checkstyle.xml")
}

pmd {
    toolVersion = "7.21.0"
}


tasks.named("compileJava") {
    dependsOn("spotlessApply")
}


tasks.withType<Checkstyle>().configureEach {
    dependsOn("spotlessApply")
}

// configure<com.github.spotbugs.snom.SpotBugsExtension> {
//     toolVersion = "4.9.7"
// }

tasks.register("checkQuality") {
    dependsOn("checkstyleMain", "checkstyleTest", "pmdMain", "pmdTest", "spotlessCheck")
}

tasks.withType<Test> {
    useJUnitPlatform()
}