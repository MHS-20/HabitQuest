package compose.project.demo

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

sealed interface AuthResult {
    data class Success(val token: String, val userId: String?) : AuthResult
    data class Error(val message: String) : AuthResult
}

class AuthRepository {
    private val client = HttpClient(createHttpEngine()) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        val response = runCatching {
            client.post("${edgeServiceBaseUrl()}/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("email", JsonPrimitive(email))
                        put("password", JsonPrimitive(password))
                    }
                )
            }
        }.getOrElse {
            return AuthResult.Error("Unable to contact edge-service")
        }

        return when (response.status) {
            HttpStatusCode.OK -> mapSuccess(response.body<JsonObject>())
            HttpStatusCode.Unauthorized -> AuthResult.Error("Invalid credentials")
            HttpStatusCode.BadRequest -> AuthResult.Error("Check email and password")
            else -> AuthResult.Error("Login error (${response.status.value})")
        }
    }

    suspend fun register(name: String, email: String, password: String): AuthResult {
        val response = runCatching {
            client.post("${edgeServiceBaseUrl()}/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    buildJsonObject {
                        put("name", JsonPrimitive(name))
                        put("email", JsonPrimitive(email))
                        put("password", JsonPrimitive(password))
                    }
                )
            }
        }.getOrElse {
            return AuthResult.Error("Unable to contact edge-service")
        }

        return when (response.status) {
            HttpStatusCode.Created -> mapSuccess(response.body<JsonObject>())
            HttpStatusCode.Conflict -> AuthResult.Error("Email already registered")
            HttpStatusCode.BadRequest -> AuthResult.Error("Invalid data")
            else -> AuthResult.Error("Registration error (${response.status.value})")
        }
    }

    private fun mapSuccess(body: JsonObject): AuthResult {
        val token = body["token"]?.jsonPrimitive?.contentOrNull
            ?: return AuthResult.Error("Token missing in response")
        val userId = body["userId"]
            ?.jsonObject
            ?.get("value")
            ?.jsonPrimitive
            ?.contentOrNull
        return AuthResult.Success(token = token, userId = userId)
    }
}

expect fun edgeServiceBaseUrl(): String
expect fun createHttpEngine(): HttpClientEngineFactory<*>

