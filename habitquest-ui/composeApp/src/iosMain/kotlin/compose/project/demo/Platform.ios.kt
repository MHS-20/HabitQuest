package compose.project.demo

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun edgeServiceBaseUrl(): String = "http://localhost:9000"

actual fun createHttpEngine(): HttpClientEngineFactory<*> = Darwin
