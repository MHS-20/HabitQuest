package habitquest.notification.infrastructure.repository;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserEmailRepository implements UserEmailRepository {

    private final Map<String, String> store = new ConcurrentHashMap<>();

    public void save(String userId, String email) {
        store.put(userId, email);
    }

    public Optional<String> findEmailByUserId(String userId) {
        return Optional.ofNullable(store.get(userId));
    }
}