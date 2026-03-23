package habitquest.edge.infrastructure;

import static org.assertj.core.api.Assertions.*;

import common.ddd.Id;
import habitquest.edge.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"checkstyle:MethodName", "checkstyle:VisibilityModifier"})
class JwtServiceTest {

  private static final String SECRET = "test-secret-key-minimum-32-chars";
  private static final long EXPIRATION = 3600L;

  private JwtService jwtService;
  private User user;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(SECRET, EXPIRATION);
    user = new User(new Id<>("user-abc"), "mario rossi", "mario@example.com", "$2a$hashed");
  }

  // ── generateToken ─────────────────────────────────────────────────────────

  @Test
  @DisplayName("token generato contiene userId, email e role come claims")
  void generateToken_containsExpectedClaims() {
    String token = jwtService.generateToken(user);

    Claims claims = jwtService.validateAndExtract(token);

    assertThat(claims.getSubject()).isEqualTo("user-abc");
    assertThat(claims.get("email", String.class)).isEqualTo("mario@example.com");
  }

  // ── isValid ───────────────────────────────────────────────────────────────

  @Test
  @DisplayName("token appena generato è valido")
  void isValid_freshToken_returnsTrue() {
    String token = jwtService.generateToken(user);
    assertThat(jwtService.isValid(token)).isTrue();
  }

  @Test
  @DisplayName("stringa vuota non è valida")
  void isValid_emptyString_returnsFalse() {
    assertThat(jwtService.isValid("")).isFalse();
  }

  @Test
  @DisplayName("token malformato non è valido")
  void isValid_malformedToken_returnsFalse() {
    assertThat(jwtService.isValid("not.a.valid.jwt")).isFalse();
  }

  @Test
  @DisplayName("token firmato con secret diverso non è valido")
  void isValid_differentSecret_returnsFalse() {
    JwtService otherService = new JwtService("altro-secret-diverso-minimo-32-chars!!", EXPIRATION);
    String tokenFromOther = otherService.generateToken(user);

    assertThat(jwtService.isValid(tokenFromOther)).isFalse();
  }

  @Test
  @DisplayName("token scaduto non è valido")
  void isValid_expiredToken_returnsFalse() {
    JwtService shortLived = new JwtService(SECRET, -1L); // già scaduto
    String expiredToken = shortLived.generateToken(user);

    assertThat(jwtService.isValid(expiredToken)).isFalse();
  }

  // ── extractUserId / extractEmail ──────────────────────────────────────────

  @Test
  @DisplayName("extractUserId restituisce il subject corretto")
  void extractUserId_returnsSubject() {
    String token = jwtService.generateToken(user);
    assertThat(jwtService.extractUserId(token)).isEqualTo("user-abc");
  }

  @Test
  @DisplayName("extractEmail restituisce l'email corretta")
  void extractEmail_returnsEmail() {
    String token = jwtService.generateToken(user);
    assertThat(jwtService.extractEmail(token)).isEqualTo("mario@example.com");
  }

  @Test
  @DisplayName("validateAndExtract su token invalido lancia JwtException")
  void validateAndExtract_invalidToken_throws() {
    assertThatThrownBy(() -> jwtService.validateAndExtract("invalid"))
        .isInstanceOf(JwtException.class);
  }
}
