plugins {
    java
    id("com.diffplug.spotless")
    // id("com.github.spotbugs")
    checkstyle
    pmd
}

val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
private val checkstyle    = catalog.findVersion("checkstyle").get().requiredVersion
private val pmd = catalog.findVersion("pmd").get().requiredVersion
private val spotbugs     = catalog.findVersion("spotbugs").get().requiredVersion

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
    toolVersion = checkstyle
    configFile = file("${rootProject.projectDir}/config/checkstyle/checkstyle.xml")
}

pmd {
    toolVersion = pmd
}


tasks.named("compileJava") {
    dependsOn("spotlessApply")
}


tasks.withType<Checkstyle>().configureEach {
    dependsOn("spotlessApply")
}

// configure<com.github.spotbugs.snom.SpotBugsExtension> {
//     toolVersion = "$spotbugs"
// }

tasks.register("checkQuality") {
    dependsOn("checkstyleMain", "checkstyleTest", "pmdMain", "pmdTest", "spotlessCheck")
}

tasks.withType<Test> {
    useJUnitPlatform()
}