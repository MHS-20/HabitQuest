package habitquest.edge.infrastructure;

import habitquest.edge.application.AuthService;
import habitquest.edge.application.AuthService.AuthResponse;
import habitquest.edge.domain.UserExceptions.InvalidCredentialsException;
import habitquest.edge.domain.UserExceptions.UserAlreadyExistsException;
import habitquest.edge.domain.UserExceptions.UserNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  // ── POST /auth/register ───────────────────────────────────────────────────
  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    try {
      AuthResponse response = authService.register(request.email(), request.password());
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (UserAlreadyExistsException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  // ── POST /auth/login ──────────────────────────────────────────────────────
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    try {
      return ResponseEntity.ok(authService.login(request.email(), request.password()));
    } catch (UserNotFoundException | InvalidCredentialsException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  // ── POST /auth/validate ───────────────────────────────────────────────────
  @PostMapping("/validate")
  public ResponseEntity<ValidateResponse> validate(
      @RequestHeader("Authorization") String authHeader) {
    String token = extractBearer(authHeader);
    if (token == null) {
      return ResponseEntity.badRequest().build();
    }
    return authService.validateToken(token)
        ? ResponseEntity.ok(new ValidateResponse(true))
        : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ValidateResponse(false));
  }

  // ── helpers ───────────────────────────────────────────────────────────────
  private String extractBearer(String header) {
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }

  // ── request / response records ────────────────────────────────────────────
  public record RegisterRequest(
      @Email @NotBlank String email,
      @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
          String password) {}

  public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

  public record ValidateResponse(boolean valid) {}
}
