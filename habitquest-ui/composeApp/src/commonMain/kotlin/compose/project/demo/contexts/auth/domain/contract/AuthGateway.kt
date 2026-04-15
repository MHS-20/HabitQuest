package compose.project.demo.contexts.auth.domain.contract

import compose.project.demo.contexts.auth.domain.model.AuthResult

interface AuthGateway {
    suspend fun login(
        email: String,
        password: String,
    ): AuthResult

    suspend fun register(
        name: String,
        email: String,
        password: String,
    ): AuthResult
}
