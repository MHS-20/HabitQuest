package habitquest.edge.application.port.in;

import habitquest.edge.domain.AuthResponse;

public interface AuthService {

  AuthResponse register(String name, String email, String rawPassword);

  AuthResponse login(String email, String rawPassword);

  boolean validateToken(String token);
}
