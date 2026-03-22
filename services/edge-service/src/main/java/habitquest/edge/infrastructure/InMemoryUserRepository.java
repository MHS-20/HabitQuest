package habitquest.edge.infrastructure;

import common.hexagonal.Adapter;
import habitquest.edge.application.UserRepository;
import habitquest.edge.domain.User;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Adapter
@Repository
public class InMemoryUserRepository implements UserRepository {

  private final Map<String, User> byEmail = new ConcurrentHashMap<>();
  private final Map<String, User> byId = new ConcurrentHashMap<>();

  @Override
  public Optional<User> findByEmail(String email) {
    return Optional.ofNullable(byEmail.get(email.toLowerCase(Locale.getDefault())));
  }

  @Override
  public Optional<User> findById(String id) {
    return Optional.ofNullable(byId.get(id));
  }

  @Override
  public boolean existsByEmail(String email) {
    return byEmail.containsKey(email.toLowerCase(Locale.getDefault()));
  }

  @Override
  public User save(User user) {
    byEmail.put(user.getEmail().toLowerCase(Locale.getDefault()), user);
    byId.put(user.getId().value(), user);
    return user;
  }

  @Override
  public void deleteById(String id) {
    User user = byId.remove(id);
    if (user != null) {
      byEmail.remove(user.getEmail().toLowerCase(Locale.getDefault()));
    }
  }
}
