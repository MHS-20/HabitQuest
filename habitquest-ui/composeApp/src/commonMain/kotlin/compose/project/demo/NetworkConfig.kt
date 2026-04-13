package compose.project.demo

import io.ktor.client.engine.HttpClientEngineFactory

expect fun edgeServiceBaseUrl(): String

expect fun createHttpEngine(): HttpClientEngineFactory<*>
