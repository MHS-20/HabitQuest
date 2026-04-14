package habitquest.edge.security;

import static org.assertj.core.api.Assertions.*;

import common.ddd.Id;
import habitquest.edge.domain.User;
import habitquest.edge.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(
    properties = {
      "app.jwt.secret=test-secret-key-minimum-32-chars!!",
      "app.jwt.expiration-seconds=3600",
      "services.avatar.base-url=http://localhost:8081"
    })
@SuppressWarnings({"checkstyle:MethodName", "checkstyle:VisibilityModifier"})
class JwtAuthenticationFilterTest {

  @Autowired WebTestClient webTestClient;
  private final JwtService jwtService = new JwtService("test-secret-key-minimum-32-chars!!", 3600L);
  private final User user =
      new User(new Id<>("user-1"), "mario rossi", "mario@example.com", "hash");

  // Common constants for repeated literals
  private static final String AVATAR_PATH = "/api/v1/avatars/1";
  private static final String AUTH_HEADER = "Authorization";

  // ── authenticated requests ────────────────────────────────────────────

  @Test
  @DisplayName("token valido → non 401/403 (filtro ha autenticato la richiesta)")
  void filter_validToken_setsAuthentication() {
    String token = jwtService.generateToken(user);

    // Request can fail for downstream reasons, but it must not be blocked by security.
    webTestClient
        .get()
        .uri(AVATAR_PATH)
        .header(AUTH_HEADER, "Bearer " + token)
        .exchange()
        .expectStatus()
        .value(
            status ->
                assertThat(status)
                    .isNotIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value()));
  }

  // ── unauthenticated requests ──────────────────────────────────────────

  @Test
  @DisplayName("nessun token su endpoint protetto → 401")
  void filter_noToken_protectedEndpoint_returns401() {
    webTestClient.get().uri(AVATAR_PATH).exchange().expectStatus().isUnauthorized();
  }

  @Test
  @DisplayName("token malformato → 401")
  void filter_malformedToken_returns401() {
    webTestClient
        .get()
        .uri(AVATAR_PATH)
        .header(AUTH_HEADER, "Bearer not.a.real.token")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  @DisplayName("token firmato con chiave diversa → 401")
  void filter_tokenSignedWithWrongKey_returns401() {
    JwtService wrongKey = new JwtService("completely-different-key-32chars!!", 3600L);
    String token = wrongKey.generateToken(user);

    webTestClient
        .get()
        .uri(AVATAR_PATH)
        .header(AUTH_HEADER, "Bearer " + token)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  @Test
  @DisplayName("token scaduto → 401")
  void filter_expiredToken_returns401() {
    JwtService shortLived = new JwtService("test-secret-key-minimum-32-chars!!", 0L);
    String token = shortLived.generateToken(user);

    webTestClient
        .get()
        .uri(AVATAR_PATH)
        .header(AUTH_HEADER, "Bearer " + token)
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }

  // ── public paths ──────────────────────────────────────────────────────

  @Test
  @DisplayName("/auth/register è pubblico → non 401/403")
  void filter_publicPath_register_noTokenNeeded() {
    webTestClient
        .post()
        .uri("/auth/register")
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .bodyValue("{" + "\"email\":\"test@test.com\"," + "\"password\":\"password123\"}")
        .exchange()
        .expectStatus()
        .value(
            status ->
                assertThat(status)
                    .isNotIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value()));
  }

  @Test
  @DisplayName("/auth/login è pubblico: senza token la risposta dipende dalle credenziali")
  void filter_publicPath_login_noTokenNeeded() {
    webTestClient
        .post()
        .uri("/auth/login")
        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
        .bodyValue("{" + "\"email\":\"test@test.com\"," + "\"password\":\"password123\"}")
        .exchange()
        .expectStatus()
        .isUnauthorized();
  }
}
