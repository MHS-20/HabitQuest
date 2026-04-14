package habitquest.quest.application.port.out;

import common.ddd.Id;
import common.hexagonal.OutBoundPort;
import habitquest.quest.domain.Avatar;

@OutBoundPort
public interface AvatarClientPort {
  void earnMoney(Id<Avatar> avatarId, int amount);

  void applyDamage(Id<Avatar> avatarId, int amount);
}
