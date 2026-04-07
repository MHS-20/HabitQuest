package habitquest.tracking.application;

import common.ddd.Id;
import common.hexagonal.OutBoundPort;
import habitquest.tracking.domain.Avatar;

@OutBoundPort
public interface AvatarClientPort {
  void grantExperience(Id<Avatar> avatarId, int amount);

  void applyDamage(Id<Avatar> avatarId, int amount);
}
