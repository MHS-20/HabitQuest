package habitquest.avatar.application;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.avatar.domain.avatar.Avatar;
import java.util.Optional;

@OutBoundPort
public interface AvatarRepository extends Repository {
  Avatar save(Avatar avatar);

  Optional<Avatar> findById(String id) throws AvatarNotFoundExpection;

  void deleteById(String id) throws AvatarNotFoundExpection;
}
