package habitquest.notification.infrastructure.repository;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserEmailRepository {
    Optional<String> findEmailByUserId(String userId);
        void save(String userId, String email);

}