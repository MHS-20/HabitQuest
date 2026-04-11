package compose.project.demo

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

class WasmPlatform : Platform {
  override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun edgeServiceBaseUrl(): String = "http://localhost:9000"

actual fun createHttpEngine(): HttpClientEngineFactory<*> = Js
