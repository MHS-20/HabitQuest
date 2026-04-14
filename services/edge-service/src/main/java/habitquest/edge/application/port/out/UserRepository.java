package habitquest.edge.application.port.out;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.edge.domain.User;
import java.util.Optional;

@OutBoundPort
public interface UserRepository extends Repository {

  Optional<User> findByEmail(String email);

  Optional<User> findById(String id);

  boolean existsByEmail(String email);

  User save(User user);

  void deleteById(String id);
}
