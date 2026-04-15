package compose.project.demo.contexts.auth.application

import compose.project.demo.contexts.auth.domain.contract.AuthGateway
import compose.project.demo.contexts.auth.domain.model.AuthResult

class RegisterUseCase(
    private val authGateway: AuthGateway,
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
    ): AuthResult = authGateway.register(name = name, email = email, password = password)
}
