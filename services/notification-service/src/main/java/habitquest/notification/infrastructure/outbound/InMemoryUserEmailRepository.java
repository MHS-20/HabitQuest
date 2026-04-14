package habitquest.notification.infrastructure.outbound;

import habitquest.notification.application.port.out.UserEmailRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryUserEmailRepository implements UserEmailRepository {

  private final Map<String, String> store = new ConcurrentHashMap<>();

  public void save(String userId, String email) {
    store.put(userId, email);
  }

  public Optional<String> findEmailByUserId(String userId) {
    return Optional.ofNullable(store.get(userId));
  }

  @Override
  public void clear() {
    store.clear();
  }
}
