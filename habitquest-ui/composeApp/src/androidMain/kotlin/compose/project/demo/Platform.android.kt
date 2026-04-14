package compose.project.demo

import android.os.Build
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun edgeServiceBaseUrl(): String = "http://10.0.2.2:9000"

actual fun createHttpEngine(): HttpClientEngineFactory<*> = OkHttp
