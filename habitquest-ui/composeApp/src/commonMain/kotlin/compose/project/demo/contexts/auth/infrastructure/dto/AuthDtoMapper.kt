package compose.project.demo.contexts.auth.infrastructure.dto

import compose.project.demo.contexts.auth.domain.model.AuthResult
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun mapAuthSuccess(body: JsonObject): AuthResult {
    val token =
        body["token"]?.jsonPrimitive?.contentOrNull
            ?: return AuthResult.Error("Token missing in response")
    val userId =
        body["userId"]
            ?.jsonObject
            ?.get("value")
            ?.jsonPrimitive
            ?.contentOrNull
    return AuthResult.Success(token = token, userId = userId)
}
