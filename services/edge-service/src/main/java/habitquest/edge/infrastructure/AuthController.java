package habitquest.edge.infrastructure;

import habitquest.edge.application.AuthService;
import habitquest.edge.application.AuthService.AuthResponse;
import habitquest.edge.application.EdgeLogger;
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
  private final AvatarClient avatarClient;
  private final EdgeLogger log;

  public AuthController(AuthService authService, AvatarClient avatarClient, EdgeLogger log) {
    this.authService = authService;
    this.avatarClient = avatarClient;
    this.log = log;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
    try {
      AuthResponse response =
          authService.register(request.name, request.email(), request.password());
      avatarClient.createAvatar(response.userId().value(), request.name());
      log.info(response, "User registered successfully");
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (UserAlreadyExistsException e) {
      log.warn(e, "Registration failed: user already exists");
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } catch (AvatarCreationException e) {
      log.error(e, "Registration failed: avatar creation error", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    try {
      AuthResponse response = authService.login(request.email(), request.password());
      log.info(response, "User logged in successfully");
      return ResponseEntity.ok(response);
    } catch (UserNotFoundException | InvalidCredentialsException e) {
      log.warn(e, "Login failed: invalid credentials or user not found");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @PostMapping("/validate")
  public ResponseEntity<ValidateResponse> validate(
      @RequestHeader("Authorization") String authHeader) {
    String token = extractBearer(authHeader);
    if (token == null) {
      log.warn(
          new ValidateResponse(false),
          "Token validation failed: missing or malformed Bearer token");
      return ResponseEntity.badRequest().build();
    }
    ValidateResponse response =
        authService.validateToken(token) ? new ValidateResponse(true) : new ValidateResponse(false);
    log.info(response, "Token validation result");
    return response.valid()
        ? ResponseEntity.ok(response)
        : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
  }

  private String extractBearer(String header) {
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }

  public record RegisterRequest(
      @NotBlank String name,
      @Email @NotBlank String email,
      @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
          String password) {}

  public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

  public record ValidateResponse(boolean valid) {}
}
