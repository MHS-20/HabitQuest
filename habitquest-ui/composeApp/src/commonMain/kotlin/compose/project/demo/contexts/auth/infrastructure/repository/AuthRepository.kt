package compose.project.demo.contexts.auth.infrastructure.repository

import compose.project.demo.contexts.auth.domain.contract.AuthGateway
import compose.project.demo.contexts.auth.domain.model.AuthResult
import compose.project.demo.contexts.auth.infrastructure.dto.mapAuthSuccess
import compose.project.demo.createHttpEngine
import compose.project.demo.edgeServiceBaseUrl
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject

class AuthRepository : AuthGateway {
    private val client =
        HttpClient(createHttpEngine()) {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

    override suspend fun login(
        email: String,
        password: String,
    ): AuthResult {
        val response =
            runCatching {
                client.post("${edgeServiceBaseUrl()}/auth/login") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        buildJsonObject {
                            put("email", JsonPrimitive(email))
                            put("password", JsonPrimitive(password))
                        },
                    )
                }
            }.getOrElse {
                return AuthResult.Error("Unable to contact edge-service")
            }

        return when (response.status) {
            HttpStatusCode.OK -> mapAuthSuccess(response.body<JsonObject>())
            HttpStatusCode.Unauthorized -> AuthResult.Error("Invalid credentials")
            HttpStatusCode.BadRequest -> AuthResult.Error("Check email and password")
            else -> AuthResult.Error("Login error (${response.status.value})")
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String,
    ): AuthResult {
        val response =
            runCatching {
                client.post("${edgeServiceBaseUrl()}/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        buildJsonObject {
                            put("name", JsonPrimitive(name))
                            put("email", JsonPrimitive(email))
                            put("password", JsonPrimitive(password))
                        },
                    )
                }
            }.getOrElse {
                return AuthResult.Error("Unable to contact edge-service")
            }

        return when (response.status) {
            HttpStatusCode.Created -> mapAuthSuccess(response.body<JsonObject>())
            HttpStatusCode.Conflict -> AuthResult.Error("Email already registered")
            HttpStatusCode.BadRequest -> AuthResult.Error("Invalid data")
            else -> AuthResult.Error("Registration error (${response.status.value})")
        }
    }
}
