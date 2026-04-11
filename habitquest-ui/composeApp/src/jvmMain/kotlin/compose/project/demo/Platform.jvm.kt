package compose.project.demo

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

class JVMPlatform : Platform {
  override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun edgeServiceBaseUrl(): String = "http://localhost:9000"

actual fun createHttpEngine(): HttpClientEngineFactory<*> = OkHttp
