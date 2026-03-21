package habitquest.edge.infrastructure;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import habitquest.edge.application.AuthService;
import habitquest.edge.application.AuthService.AuthResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@SuppressWarnings({"checkstyle:MethodName", "checkstyle:VisibilityModifier"})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

  @Autowired MockMvc mockMvc;

  @MockitoBean AvatarClient avatarClient;
  @MockitoBean AuthService authService;
  @MockitoBean JwtService jwtService;

  @MockitoBean CircuitBreakerRegistry circuitBreakerRegistry;
  @MockitoBean RateLimiterRegistry rateLimiterRegistry;

  private static final String JWT_TOKEN = "eyJ.fake.token";
  private static final String USER_ID = "user-123";
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

  private io.jsonwebtoken.Claims fakeClaims() {
    User user = new User("user-1", NAME, TEST_EMAIL, "hash");
    String token = realJwtService.generateToken(user);
    return realJwtService.validateAndExtract(token);
  }

  @BeforeEach
  void stubResilienceFilter() {
    CircuitBreaker cb = CircuitBreaker.ofDefaults("test");
    RateLimiter rl = RateLimiter.ofDefaults("test");
    when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(cb);
    when(rateLimiterRegistry.rateLimiter(anyString())).thenReturn(rl);
  }

  // ── POST /auth/register ───────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /auth/register")
  class Register {

    @Test
    @DisplayName("201 con token per registrazione valida")
    void register_success_returns201() throws Exception {
      when(authService.register(NAME, TEST_EMAIL, TEST_PASSWORD))
          .thenReturn(new AuthResponse(JWT_TOKEN, USER_ID));

      mockMvc
          .perform(
              post(REGISTER_PATH)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(credentialsJson(NAME, TEST_EMAIL, TEST_PASSWORD)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.token").value(JWT_TOKEN))
          .andExpect(jsonPath("$.userId").value(USER_ID));
    }

    @Test
    @DisplayName("chiama avatarClient con userId da authService e name dalla request")
    void register_success_callsAvatarClientWithCorrectArgs() throws Exception {
      when(authService.register(NAME, TEST_EMAIL, TEST_PASSWORD))
          .thenReturn(new AuthResponse(JWT_TOKEN, USER_ID));

      mockMvc.perform(
          post(REGISTER_PATH)
              .contentType(MediaType.APPLICATION_JSON)
              .content(credentialsJson(NAME, TEST_EMAIL, TEST_PASSWORD)));

      verify(avatarClient).createAvatar(USER_ID, NAME);
    }

    @Test
    @DisplayName("non chiama avatarClient se la registrazione fallisce")
    void register_authServiceFails_doesNotCallAvatarClient() throws Exception {
      when(authService.register(anyString(), anyString(), anyString()))
          .thenThrow(new UserAlreadyExistsException(TEST_EMAIL));

      mockMvc.perform(
          post(REGISTER_PATH)
              .contentType(MediaType.APPLICATION_JSON)
              .content(credentialsJson(NAME, TEST_EMAIL, TEST_PASSWORD)));

      verify(avatarClient, never()).createAvatar(anyString(), anyString());
    }

    @Test
    @DisplayName("500 se avatarClient lancia AvatarCreationException")
    void register_avatarClientFails_returns500() throws Exception {
      when(authService.register(NAME, TEST_EMAIL, TEST_PASSWORD))
          .thenReturn(new AuthResponse(JWT_TOKEN, USER_ID));
      doThrow(new AvatarCreationException("avatar-service unreachable"))
          .when(avatarClient)
          .createAvatar(anyString(), anyString());

      mockMvc
          .perform(
              post(REGISTER_PATH)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(credentialsJson(NAME, TEST_EMAIL, TEST_PASSWORD)))
          .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("409 se email già registrata")
    void register_duplicateEmail_returns409() throws Exception {
      when(authService.register(anyString(), anyString(), anyString()))
          .thenThrow(new UserAlreadyExistsException(TEST_EMAIL));

      mockMvc
          .perform(
              post(REGISTER_PATH)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(credentialsJson(NAME, TEST_EMAIL, TEST_PASSWORD)))
          .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("400 se email non valida")
    void register_invalidEmail_returns400() throws Exception {
      mockMvc
          .perform(
              post(REGISTER_PATH)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(credentialsJson(NAME, "not-an-email", TEST_PASSWORD)))
          .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("400 se password troppo corta")
    void register_shortPassword_returns400() throws Exception {
      mockMvc
          .perform(
              post(REGISTER_PATH)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(credentialsJson(NAME, TEST_EMAIL, "short")))
          .andExpect(status().isBadRequest());
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
          .thenReturn(new AuthResponse(JWT_TOKEN, USER_ID));

      mockMvc
          .perform(
              post(LOGIN_PATH)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(credentialsJson(NAME, TEST_EMAIL, TEST_PASSWORD)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.token").value(JWT_TOKEN));
    }

    @Test
    @DisplayName("401 se utente non esiste")
    void login_userNotFound_returns401() throws Exception {
      when(authService.login(anyString(), anyString()))
          .thenThrow(new UserNotFoundException(TEST_EMAIL));

      mockMvc
          .perform(
              post(LOGIN_PATH)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(credentialsJson(NAME, TEST_EMAIL, TEST_PASSWORD)))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("401 se password errata")
    void login_wrongPassword_returns401() throws Exception {
      when(authService.login(anyString(), anyString()))
          .thenThrow(new InvalidCredentialsException());

      mockMvc
          .perform(
              post(LOGIN_PATH)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(credentialsJson(NAME, TEST_EMAIL, "wrongpass")))
          .andExpect(status().isUnauthorized());
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

      mockMvc
          .perform(post("/auth/validate").header("Authorization", "Bearer " + JWT_TOKEN))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    @DisplayName("401 con valid=false per token non valido")
    void validate_invalidToken_returns401() throws Exception {
      when(jwtService.validateAndExtract("bad.token"))
          .thenThrow(new io.jsonwebtoken.MalformedJwtException("token non valido"));
      when(authService.validateToken("bad.token")).thenReturn(false);

      mockMvc
          .perform(post("/auth/validate").header("Authorization", "Bearer bad.token"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("400 se header Authorization mancante")
    void validate_missingHeader_returns400() throws Exception {
      mockMvc.perform(post("/auth/validate")).andExpect(status().isBadRequest());
    }
  }
}
