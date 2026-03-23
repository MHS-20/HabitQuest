package habitquest.edge.domain;

import common.ddd.Factory;
import common.ddd.Id;
import org.springframework.stereotype.Component;

@Component
public class UserFactory implements Factory {

  private final IdGenerator idGenerator;

  public UserFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public User create(String name, String email, String passwordHash) {
    if (email == null || !email.contains("@")) {
      throw new IllegalArgumentException("Invalid email: " + email);
    }
    if (passwordHash == null || passwordHash.isBlank()) {
      throw new IllegalArgumentException("Password hash must not be blank");
    }
    return new User(new Id<>(idGenerator.nextId()), name, email, passwordHash);
  }
}
