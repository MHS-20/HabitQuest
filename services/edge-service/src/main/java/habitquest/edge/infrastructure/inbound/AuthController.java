package habitquest.edge.infrastructure.inbound;

import habitquest.edge.application.port.in.AuthService;
import habitquest.edge.application.port.out.EdgeLogger;
import habitquest.edge.domain.AuthResponse;
import habitquest.edge.domain.UserExceptions.InvalidCredentialsException;
import habitquest.edge.domain.UserExceptions.UserAlreadyExistsException;
import habitquest.edge.domain.UserExceptions.UserNotFoundException;
import habitquest.edge.infrastructure.AvatarCreationException;
import habitquest.edge.infrastructure.outbound.AvatarClient;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
  public Mono<ResponseEntity<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
    return Mono.fromCallable(
            () -> authService.register(request.name, request.email(), request.password()))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(
            response ->
                avatarClient
                    .createAvatar(response.userId().value(), request.name())
                    .thenReturn(response))
        .map(
            response -> {
              log.info(response, "User registered successfully");
              return ResponseEntity.status(HttpStatus.CREATED).body(response);
            })
        .onErrorResume(
            UserAlreadyExistsException.class,
            e -> {
              log.warn(e, "Registration failed: user already exists");
              return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
            })
        .onErrorResume(
            AvatarCreationException.class,
            e -> {
              log.error(e, "Registration failed: avatar creation error", e);
              return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
  }

  @PostMapping("/login")
  public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    return Mono.fromCallable(() -> authService.login(request.email(), request.password()))
        .subscribeOn(Schedulers.boundedElastic())
        .map(
            response -> {
              log.info(response, "User logged in successfully");
              return ResponseEntity.ok(response);
            })
        .onErrorResume(
            e -> e instanceof UserNotFoundException || e instanceof InvalidCredentialsException,
            e -> {
              log.warn(e, "Login failed: invalid credentials or user not found");
              return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
            });
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
