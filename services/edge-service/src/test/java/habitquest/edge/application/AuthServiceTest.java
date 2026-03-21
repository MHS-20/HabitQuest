package habitquest.edge.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import habitquest.edge.domain.User;
import habitquest.edge.domain.UserExceptions.InvalidCredentialsException;
import habitquest.edge.domain.UserExceptions.UserAlreadyExistsException;
import habitquest.edge.domain.UserExceptions.UserNotFoundException;
import habitquest.edge.domain.UserFactory;
import habitquest.edge.infrastructure.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"checkstyle:MethodName", "checkstyle:VisibilityModifier"})
class AuthServiceTest {

  @Mock UserRepository userRepository;
  @Mock JwtService jwtService;
  @Mock PasswordEncoder passwordEncoder;
  @Mock UserFactory userFactory;
  @Mock UserNotifier userNotifier;

  @InjectMocks AuthService authService;

  private static final String EMAIL = "mario@example.com";
  private static final String RAW_PASSWORD = "password123";
  private static final String HASHED_PASSWORD = "$2a$10$hashedpassword";
  private static final String JWT_TOKEN = "eyJ.fake.token";
  private static final String USER_ID = "user-123";
  private static final String NAME = "Mario Rossi";
  private static final User FAKE_USER = new User(USER_ID, NAME, EMAIL, HASHED_PASSWORD);

  // ── register ──────────────────────────────────────────────────────────────
  @Nested
  @DisplayName("register()")
  class Register {

    @Test
    @DisplayName("registrazione ok: salva utente e restituisce token")
    void register_success() {
      when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
      when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
      when(userFactory.create(NAME, EMAIL, HASHED_PASSWORD)).thenReturn(FAKE_USER);
      when(userRepository.save(FAKE_USER)).thenReturn(FAKE_USER);
      when(jwtService.generateToken(FAKE_USER)).thenReturn(JWT_TOKEN);

      AuthService.AuthResponse response = authService.register(NAME, EMAIL, RAW_PASSWORD);

      assertThat(response.token()).isEqualTo(JWT_TOKEN);
      assertThat(response.userId()).isEqualTo(USER_ID);
      verify(userRepository).save(FAKE_USER);
    }

    @Test
    @DisplayName("email già registrata → UserAlreadyExistsException")
    void register_duplicateEmail_throws() {
      when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

      assertThatThrownBy(() -> authService.register(NAME, EMAIL, RAW_PASSWORD))
          .isInstanceOf(UserAlreadyExistsException.class);

      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("password viene sempre hashata prima del salvataggio")
    void register_passwordIsHashed() {
      when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
      when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
      when(userFactory.create(NAME, EMAIL, HASHED_PASSWORD)).thenReturn(FAKE_USER);
      when(userRepository.save(FAKE_USER)).thenReturn(FAKE_USER);
      when(jwtService.generateToken(FAKE_USER)).thenReturn(JWT_TOKEN);

      authService.register(NAME, EMAIL, RAW_PASSWORD);

      verify(passwordEncoder).encode(RAW_PASSWORD);
      verify(userFactory).create(NAME, EMAIL, HASHED_PASSWORD);
    }
  }

  // ── login ─────────────────────────────────────────────────────────────────
  @Nested
  @DisplayName("login()")
  class Login {

    private User existingUser;

    @BeforeEach
    void setUp() {
      existingUser = new User(USER_ID, NAME, EMAIL, HASHED_PASSWORD);
    }

    @Test
    @DisplayName("credenziali corrette → restituisce token")
    void login_success() {
      when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
      when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
      when(jwtService.generateToken(existingUser)).thenReturn(JWT_TOKEN);

      AuthService.AuthResponse response = authService.login(EMAIL, RAW_PASSWORD);

      assertThat(response.token()).isEqualTo(JWT_TOKEN);
      assertThat(response.userId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("utente non trovato → UserNotFoundException")
    void login_userNotFound_throws() {
      when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> authService.login(EMAIL, RAW_PASSWORD))
          .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("password errata → InvalidCredentialsException")
    void login_wrongPassword_throws() {
      when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser));
      when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(false);

      assertThatThrownBy(() -> authService.login(EMAIL, RAW_PASSWORD))
          .isInstanceOf(InvalidCredentialsException.class);
    }
  }

  // ── validateToken ─────────────────────────────────────────────────────────
  @Nested
  @DisplayName("validateToken()")
  class ValidateToken {

    @Test
    @DisplayName("token valido → true")
    void validateToken_valid() {
      when(jwtService.isValid(JWT_TOKEN)).thenReturn(true);
      assertThat(authService.validateToken(JWT_TOKEN)).isTrue();
    }

    @Test
    @DisplayName("token scaduto o malformato → false")
    void validateToken_invalid() {
      when(jwtService.isValid("bad.token")).thenReturn(false);
      assertThat(authService.validateToken("bad.token")).isFalse();
    }
  }
}
