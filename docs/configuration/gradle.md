# Build Logic Module Documentation

## Overview

This project uses a **Gradle composite build** with a dedicated `build-logic` included build. 
This pattern centralizes all build configuration — plugins, conventions, and toolchain settings — in one place, 
making it easy to maintain consistency across every subproject without repeating boilerplate.

## Version Management

The project uses **two TOML version catalogs**:

### `gradle/libs.versions.toml`
Used for backend/service dependencies. Manages versions for:
- Spring Boot and Spring Cloud
- Testcontainers
- Micrometer observability stack
- Static analysis tools: Checkstyle, PMD, Spotless, SpotBugs

### `gradle/ui.versions.toml`
Used for KMP and Android UI modules. Manages versions for:
- Kotlin (including the multiplatform and Compose compiler plugins)
- Android Gradle Plugin (AGP)
- Compose Multiplatform
- Detekt and ktlint (Kotlin linters)

### Versions Resolution

The `build-logic/settings.gradle.kts` imports **both** catalogs into the build-logic classpath:
The `build-logic/build.gradle.kts` then **reads plugin versions directly from the catalogs** at configuration time and pins them into the `dependencies` block:

```kotlin
val catalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
private val springBoot = catalog.findVersion("spring-boot").get().requiredVersion

dependencies {
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:$springBoot")
    // ...
}
```

This means **the TOML files are the single source of truth** for every plugin and library version across both the build-logic and the main build. 
Updating a version in one place propagates everywhere.

## Convention Plugins

### `java-conventions`
Baseline configuration for every Java service module. 
Any module that compiles Java code applies this convention first.

**What it configures:**

- **Plugins applied:** `java`, `spotless`, `spotbugs`, `checkstyle`, `pmd`
- **Java toolchain:** Forces Java 21 for all compilation and execution, regardless of the JDK installed locally or on CI.
- **Spotless:** Formats all `src/**/*.java` files using `googleJavaFormat`, removes unused imports, trims trailing whitespace, and ensures files end with a newline. Build artifacts and generated sources are excluded.
- **Checkstyle:** Configured with a shared `config/checkstyle/checkstyle.xml` at the root. The version is pinned from the `libs` catalog.
- **PMD:** Applied and version-pinned from the `libs` catalog.
- **SpotBugs:** Applied but disabled by default (individual modules can re-enable it as needed).
- **Task wiring:** a `checkQuality` task aggregates `test`, `checkstyleMain`, `checkstyleTest`, `pmdMain`, `pmdTest`, and `spotlessCheck`.

### `spring-conventions`
Full configuration for a Spring Boot microservice. 
Extends `java-conventions` and adds everything needed to build and run a Spring Boot application.

**What it configures:**

- **Plugins applied:** `java-conventions`, `org.springframework.boot`, `io.spring.dependency-management`
- **BOM imports:** Three Maven BOMs are imported for managed dependency resolution:
    - `testcontainers-bom` — all Testcontainers modules at a unified version
    - `spring-cloud-dependencies` — all Spring Cloud modules
    - `micrometer-bom` — Micrometer core and registry modules
    - `:common` project dependency (internal shared module)
- **Default dependencies** pre-wired into every Spring service using versions from the `libs.versions.toml`
- **Plain JAR disabled:** `tasks.named<Jar>("jar") { enabled = false }` ensures only the fat executable JAR produced by the Spring Boot plugin is published, avoiding confusion between the two artifacts.


### `kmp-conventions`
Configuration for a Kotlin Multiplatform (KMP) Android application module with Compose UI and code quality tooling.

**What it configures:**

- **Plugins applied:** `detekt`, `org.jetbrains.kotlin.multiplatform`, `com.android.application`, `org.jetbrains.compose`, `org.jetbrains.kotlin.plugin.compose`, `org.jlleitschuh.gradle.ktlint`
- **Detekt:** Configured with the version from `uiLibs`, a shared config file at `config/detekt/detekt.yml`, and `buildUponDefaultConfig = true`. Auto-correction is disabled (analysis only).
- **ktlint:** Configured with the version from `uiLibs`, Android mode enabled, failure suppressed.
- **`checkQuality` task:** Aggregates `detekt` and `ktlintFormat` so that a single task can run the full Kotlin quality gate.

### `containerProject-conventions`
Configures a container or aggregator project — a project whose only job is to group subprojects together and delegate tasks to them. 
It has no sources of its own.

**What it configures:**

- Registers a **dynamic task rule** using `tasks.addRule`. When any task name is invoked on the container project, the rule automatically creates a task with that name that depends on the same-named task in every direct subproject.