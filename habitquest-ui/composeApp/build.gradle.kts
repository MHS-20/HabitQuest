import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { id("kmp-conventions") }

group = "habitquest.ui"

version = rootProject.version

kotlin {
    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    js {
        browser {
            testTask {
                // Skip browser JS tests locally unless Chrome is explicitly configured.
                enabled = providers.environmentVariable("CHROME_BIN").isPresent
            }
        }
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            testTask {
                // Keep wasm browser tests opt-in for environments with a configured browser.
                enabled = providers.environmentVariable("CHROME_BIN").isPresent
            }
        }
        binaries.executable()
    }

    sourceSets {
        androidMain.dependencies {
            implementation(uiLibs.compose.uiToolingPreview)
            implementation(uiLibs.androidx.activity.compose)
            implementation(uiLibs.ktor.client.okhttp)
            implementation(uiLibs.kalendar.kit)
        }
        commonMain.dependencies {
            implementation(uiLibs.compose.runtime)
            implementation(uiLibs.compose.foundation)
            implementation(uiLibs.compose.material3)
            implementation(uiLibs.compose.ui)
            implementation(uiLibs.compose.components.resources)
            implementation(uiLibs.compose.uiToolingPreview)
            implementation(uiLibs.androidx.lifecycle.viewmodelCompose)
            implementation(uiLibs.androidx.lifecycle.runtimeCompose)
            implementation(uiLibs.ktor.client.core)
            implementation(uiLibs.ktor.client.contentNegotiation)
            implementation(uiLibs.ktor.serialization.kotlinx.json)
            implementation(uiLibs.kotlinx.serialization.json)
            implementation(uiLibs.kotlinx.datetime)
        }
        commonTest.dependencies { implementation(uiLibs.kotlin.test) }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(uiLibs.kotlinx.coroutinesSwing)
            implementation(uiLibs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(uiLibs.ktor.client.darwin)
            implementation(uiLibs.kalendar.kit)
        }
        jsMain.dependencies { implementation(uiLibs.ktor.client.js) }
        wasmJsMain.dependencies { implementation(uiLibs.ktor.client.js) }
    }
}

android {
    namespace = "habitquest.ui"
    compileSdk =
        uiLibs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "habitquest.ui"
        minSdk =
            uiLibs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            uiLibs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = version.toString()
    }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
    buildTypes { getByName("release") { isMinifyEnabled = false } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies { debugImplementation(uiLibs.compose.uiTooling) }

compose.desktop {
    application {
        mainClass = "compose.project.demo.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "compose.project.demo"
            packageVersion = "1.0.0"
        }
    }
}
