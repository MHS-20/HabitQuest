package habitquest.edge.infrastructure;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import common.ddd.Id;
import habitquest.edge.application.AuthService;
import habitquest.edge.application.AuthService.AuthResponse;
import habitquest.edge.application.EdgeLogger;
import habitquest.edge.domain.User;
import habitquest.edge.domain.UserExceptions.InvalidCredentialsException;
import habitquest.edge.domain.UserExceptions.UserAlreadyExistsException;
import habitquest.edge.domain.UserExceptions.UserNotFoundException;
import habitquest.edge.security.JwtAuthenticationFilter;
import habitquest.edge.security.SecurityConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@WebFluxTest(AuthController.class)
@SuppressWarnings({"checkstyle:MethodName", "checkstyle:VisibilityModifier"})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

  @Autowired WebTestClient webTestClient;

  @MockitoBean AvatarClient avatarClient;
  @MockitoBean AuthService authService;
  @MockitoBean JwtService jwtService;
  @MockitoBean EdgeLogger log;

  @MockitoBean CircuitBreakerRegistry circuitBreakerRegistry;
  @MockitoBean RateLimiterRegistry rateLimiterRegistry;

  private static final String JWT_TOKEN = "eyJ.fake.token";
  private static final String USER_ID = "user-123";
  private static final Id<User> USER_ID_TYPE = new Id<>(USER_ID);
  private static final String TEST_EMAIL = "mario@example.com";
  private static final String TEST_PASSWORD = "password123";
  private static final String REGISTER_PATH = "/auth/register";
  private static final String LOGIN_PATH = "/auth/login";
  private static final String NAME = "Mario Rossi";

  // Helper to build JSON bodies for credentials
  private static String credentialsJson(String name, String email, String password) {
    return String.format(
        "{\"name\":\"%s\", \"email\":\"%s\",\"password\":\"%s\"}%n", name, email, password);
  }

  private final JwtService realJwtService =
      new JwtService("test-secret-key-minimum-32-chars!!", 3600L);

  private Claims fakeClaims() {
    User user = new User(new Id<>("user-1"), NAME, TEST_EMAIL, "hash");
    String token = realJwtService.generateToken(user);
    return realJwtService.validateAndExtract(token);
  }

  @BeforeEach
  void stubResilienceFilter() {
    CircuitBreaker cb = CircuitBreaker.ofDefaults("test");
    RateLimiter rl = RateLimiter.ofDefaults("test");
    when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(cb);
    when(rateLimiterRegistry.rateLimiter(anyString())).thenReturn(rl);
    when(avatarClient.createAvatar(anyString(), anyString())).thenReturn(Mono.empty());
  }

  // ── POST /auth/register ───────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /auth/register")
  class Register {

    @Test
    @DisplayName("201 con token per registrazione valida")
    void register_success_returns201() throws Exception {
      when(authService.register(NAME, TEST_EMAIL, TEST_PASSWORD))
          .thenReturn(new AuthResponse(JWT_TOKEN, USER_ID_TYPE));

      webTestClient
          .post()
          .uri(REGISTER_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(credentialsJson(NAME, TEST_EMAIL, TEST_PASSWORD))
          .exchange()
          .expectStatus()
          .isCreated()
          .expectBody()
          .jsonPath("$.token")
          .isEqualTo(JWT_TOKEN)
          .jsonPath("$.userId.value")
          .isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("chiama avatarClient con userId da authService e name dalla request")
    void register_success_callsAvatarClientWithCorrectArgs() throws Exception {
      when(authService.register(NAME, TEST_EMAIL, TEST_PASSWORD))
          .thenReturn(new AuthResponse(JWT_TOKEN, USER_ID_TYPE));

      webTestClient
          .post()
          .uri(REGISTER_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(credentialsJson(NAME, TEST_EMAIL, TEST_PASSWORD))
          .exchange()
          .expectStatus()
          .isCreated();

      verify(avatarClient).createAvatar(USER_ID, NAME);
    }

    @Test
    @DisplayName("non chiama avatarClient se la registrazione fallisce")
    void register_authServiceFails_doesNotCallAvatarClient() throws Exception {
      when(authService.register(anyString(), anyString(), anyString()))
          .thenThrow(new UserAlreadyExistsException(TEST_EMAIL));

      webTestClient
          .post()
          .uri(REGISTER_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(credentialsJson(NAME, TEST_EMAIL, TEST_PASSWORD))
          .exchange()
          .expectStatus()
          .isEqualTo(409);

      verify(avatarClient, never()).createAvatar(anyString(), anyString());
    }

    @Test
    @DisplayName("500 se avatarClient lancia AvatarCreationException")
    void register_avatarClientFails_returns500() throws Exception {
      when(authService.register(NAME, TEST_EMAIL, TEST_PASSWORD))
          .thenReturn(new AuthResponse(JWT_TOKEN, USER_ID_TYPE));
      when(avatarClient.createAvatar(anyString(), anyString()))
          .thenReturn(Mono.error(new AvatarCreationException("avatar-service unreachable")));

      webTestClient
          .post()
          .uri(REGISTER_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(credentialsJson(NAME, TEST_EMAIL, TEST_PASSWORD))
          .exchange()
          .expectStatus()
          .is5xxServerError();
    }

    @Test
    @DisplayName("409 se email già registrata")
    void register_duplicateEmail_returns409() throws Exception {
      when(authService.register(anyString(), anyString(), anyString()))
          .thenThrow(new UserAlreadyExistsException(TEST_EMAIL));

      webTestClient
          .post()
          .uri(REGISTER_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(credentialsJson(NAME, TEST_EMAIL, TEST_PASSWORD))
          .exchange()
          .expectStatus()
          .isEqualTo(409);
    }

    @Test
    @DisplayName("400 se email non valida")
    void register_invalidEmail_returns400() throws Exception {
      webTestClient
          .post()
          .uri(REGISTER_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(credentialsJson(NAME, "not-an-email", TEST_PASSWORD))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }

    @Test
    @DisplayName("400 se password troppo corta")
    void register_shortPassword_returns400() throws Exception {
      webTestClient
          .post()
          .uri(REGISTER_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(credentialsJson(NAME, TEST_EMAIL, "short"))
          .exchange()
          .expectStatus()
          .isBadRequest();
    }
  }

  // ── POST /auth/login ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /auth/login")
  class Login {

    @Test
    @DisplayName("200 con token per credenziali corrette")
    void login_success_returns200() throws Exception {
      when(authService.login(TEST_EMAIL, TEST_PASSWORD))
          .thenReturn(new AuthResponse(JWT_TOKEN, USER_ID_TYPE));

      webTestClient
          .post()
          .uri(LOGIN_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(credentialsJson(NAME, TEST_EMAIL, TEST_PASSWORD))
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.token")
          .isEqualTo(JWT_TOKEN);
    }

    @Test
    @DisplayName("401 se utente non esiste")
    void login_userNotFound_returns401() throws Exception {
      when(authService.login(anyString(), anyString()))
          .thenThrow(new UserNotFoundException(TEST_EMAIL));

      webTestClient
          .post()
          .uri(LOGIN_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(credentialsJson(NAME, TEST_EMAIL, TEST_PASSWORD))
          .exchange()
          .expectStatus()
          .isUnauthorized();
    }

    @Test
    @DisplayName("401 se password errata")
    void login_wrongPassword_returns401() throws Exception {
      when(authService.login(anyString(), anyString()))
          .thenThrow(new InvalidCredentialsException());

      webTestClient
          .post()
          .uri(LOGIN_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(credentialsJson(NAME, TEST_EMAIL, "wrongpass"))
          .exchange()
          .expectStatus()
          .isUnauthorized();
    }
  }

  // ── POST /auth/validate ───────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /auth/validate")
  class Validate {

    @Test
    @DisplayName("200 con valid=true per token valido")
    void validate_validToken_returns200() throws Exception {
      when(jwtService.validateAndExtract(JWT_TOKEN)).thenReturn(fakeClaims());
      when(authService.validateToken(JWT_TOKEN)).thenReturn(true);

      webTestClient
          .post()
          .uri("/auth/validate")
          .header("Authorization", "Bearer " + JWT_TOKEN)
          .exchange()
          .expectStatus()
          .isOk()
          .expectBody()
          .jsonPath("$.valid")
          .isEqualTo(true);
    }

    @Test
    @DisplayName("401 con valid=false per token non valido")
    void validate_invalidToken_returns401() throws Exception {
      when(jwtService.validateAndExtract("bad.token"))
          .thenThrow(new io.jsonwebtoken.MalformedJwtException("token non valido"));
      when(authService.validateToken("bad.token")).thenReturn(false);

      webTestClient
          .post()
          .uri("/auth/validate")
          .header("Authorization", "Bearer bad.token")
          .exchange()
          .expectStatus()
          .isUnauthorized();
    }

    @Test
    @DisplayName("400 se header Authorization mancante")
    void validate_missingHeader_returns400() throws Exception {
      webTestClient.post().uri("/auth/validate").exchange().expectStatus().isBadRequest();
    }
  }
}
