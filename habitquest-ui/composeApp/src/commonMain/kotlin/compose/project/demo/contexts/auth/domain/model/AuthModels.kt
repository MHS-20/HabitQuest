package compose.project.demo.contexts.auth.domain.model

sealed interface AuthResult {
  data class Success(val token: String, val userId: String?) : AuthResult

  data class Error(val message: String) : AuthResult
}
