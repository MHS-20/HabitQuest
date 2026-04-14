package habitquest.edge.application.service;

import common.hexagonal.InBoundPort;
import habitquest.edge.application.port.in.AuthService;
import habitquest.edge.application.port.out.UserNotifier;
import habitquest.edge.application.port.out.UserRepository;
import habitquest.edge.domain.AuthResponse;
import habitquest.edge.domain.User;
import habitquest.edge.domain.UserExceptions.InvalidCredentialsException;
import habitquest.edge.domain.UserExceptions.UserAlreadyExistsException;
import habitquest.edge.domain.UserExceptions.UserNotFoundException;
import habitquest.edge.domain.UserFactory;
import habitquest.edge.infrastructure.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@InBoundPort
@Service
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;
  private final UserFactory userFactory;
  private final UserNotifier userNotifier;

  public AuthServiceImpl(
      UserRepository userRepository,
      JwtService jwtService,
      PasswordEncoder passwordEncoder,
      UserFactory userFactory,
      UserNotifier userNotifier) {
    this.userRepository = userRepository;
    this.jwtService = jwtService;
    this.passwordEncoder = passwordEncoder;
    this.userFactory = userFactory;
    this.userNotifier = userNotifier;
  }

  @Override
  public AuthResponse register(String name, String email, String rawPassword) {
    if (userRepository.existsByEmail(email)) {
      throw new UserAlreadyExistsException(email);
    }

    String hash = passwordEncoder.encode(rawPassword);
    User user = userRepository.save(userFactory.create(name, email, hash));
    userNotifier.notifyUserRegistered(user);
    return new AuthResponse(jwtService.generateToken(user), user.getId());
  }

  @Override
  public AuthResponse login(String email, String rawPassword) {
    User user =
        userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(email));
    if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
      throw new InvalidCredentialsException();
    }
    return new AuthResponse(jwtService.generateToken(user), user.getId());
  }

  @Override
  public boolean validateToken(String token) {
    return jwtService.isValid(token);
  }
}
