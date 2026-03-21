package habitquest.notification.infrastructure.repository;

import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEmailRepository {
  Optional<String> findEmailByUserId(String userId);

  void save(String userId, String email);

  void clear();
}
