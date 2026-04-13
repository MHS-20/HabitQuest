package compose.project.demo.contexts.auth.application

import compose.project.demo.contexts.auth.domain.contract.AuthGateway
import compose.project.demo.contexts.auth.domain.model.AuthResult

class LoginUseCase(private val authGateway: AuthGateway) {
  suspend operator fun invoke(email: String, password: String): AuthResult {
    return authGateway.login(email = email, password = password)
  }
}
