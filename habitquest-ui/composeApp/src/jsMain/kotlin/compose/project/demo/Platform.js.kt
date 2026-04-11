package compose.project.demo

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

class JsPlatform : Platform {
  override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun edgeServiceBaseUrl(): String = "http://localhost:9000"

actual fun createHttpEngine(): HttpClientEngineFactory<*> = Js
