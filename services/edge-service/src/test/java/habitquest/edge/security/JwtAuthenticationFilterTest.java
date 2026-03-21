package habitquest.edge.security;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import habitquest.edge.domain.User;
import habitquest.edge.infrastructure.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@Import({
  JwtAuthenticationFilter.class,
  SecurityConfig.class,
  JwtAuthenticationFilterTest.TestConfig.class
})
@TestPropertySource(
    properties = {
      "app.jwt.secret=test-secret-key-minimum-32-chars!!",
      "app.jwt.expiration-seconds=3600"
    })
@SuppressWarnings({"checkstyle:MethodName", "checkstyle:VisibilityModifier"})
class JwtAuthenticationFilterTest {

  @Autowired MockMvc mockMvc;
  private final JwtService jwtService = new JwtService("test-secret-key-minimum-32-chars!!", 3600L);
  private final User user = new User("user-1", "mario rossi", "mario@example.com", "hash");

  @Configuration
  @SuppressWarnings("PMD.TestClassWithoutTestCases")
  static class TestConfig {
    @Bean
    JwtService jwtService() {
      return new JwtService("test-secret-key-minimum-32-chars!!", 3600L);
    }
  }

  // Common constants for repeated literals
  private static final String AVATAR_PATH = "/api/v1/avatars/1";
  private static final String AUTH_HEADER = "Authorization";

  // ── authenticated requests ────────────────────────────────────────────

  @Test
  @DisplayName("token valido → non 401/403 (filtro ha autenticato la richiesta)")
  void filter_validToken_setsAuthentication() throws Exception {
    String token = jwtService.generateToken(user);

    // The gateway will fail with 500 because no downstream exists in test,
    // but the important thing is it is NOT rejected by the security layer.
    mockMvc
        .perform(get(AVATAR_PATH).header(AUTH_HEADER, "Bearer " + token))
        .andExpect(
            result ->
                assertThat(result.getResponse().getStatus())
                    .isNotIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value()));
  }

  // ── unauthenticated requests ──────────────────────────────────────────

  @Test
  @DisplayName("nessun token su endpoint protetto → 401")
  void filter_noToken_protectedEndpoint_returns401() throws Exception {
    mockMvc.perform(get(AVATAR_PATH)).andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("token malformato → 401")
  void filter_malformedToken_returns401() throws Exception {
    mockMvc
        .perform(get(AVATAR_PATH).header(AUTH_HEADER, "Bearer not.a.real.token"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("token firmato con chiave diversa → 401")
  void filter_tokenSignedWithWrongKey_returns401() throws Exception {
    JwtService wrongKey = new JwtService("completely-different-key-32chars!!", 3600L);
    String token = wrongKey.generateToken(user);

    mockMvc
        .perform(get(AVATAR_PATH).header(AUTH_HEADER, "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("token scaduto → 401")
  void filter_expiredToken_returns401() throws Exception {
    JwtService shortLived = new JwtService("test-secret-key-minimum-32-chars!!", 0L);
    String token = shortLived.generateToken(user);

    mockMvc
        .perform(get(AVATAR_PATH).header(AUTH_HEADER, "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  // ── public paths ──────────────────────────────────────────────────────

  @Test
  @DisplayName("/auth/register è pubblico → non 401/403")
  void filter_publicPath_register_noTokenNeeded() throws Exception {
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"test@test.com","password":"password123"}
                    """))
        .andExpect(
            result ->
                assertThat(result.getResponse().getStatus())
                    .isNotIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value()));
  }

  @Test
  @DisplayName("/auth/login è pubblico → non 401/403")
  void filter_publicPath_login_noTokenNeeded() throws Exception {
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"test@test.com","password":"password123"}
                    """))
        .andExpect(
            result ->
                assertThat(result.getResponse().getStatus())
                    .isNotIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value()));
  }
}
