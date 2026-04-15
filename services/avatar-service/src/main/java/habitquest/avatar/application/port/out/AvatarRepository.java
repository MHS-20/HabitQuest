package habitquest.avatar.application.port.out;

import common.ddd.Id;
import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.avatar.application.service.AvatarSearchQuery;
import habitquest.avatar.domain.avatar.Avatar;
import java.util.List;
import java.util.Optional;

@OutBoundPort
public interface AvatarRepository extends Repository {
  Avatar save(Avatar avatar);

  Optional<Avatar> findById(Id<Avatar> id);

  void deleteById(Id<Avatar> id);

  List<Avatar> search(AvatarSearchQuery criteria);
}
