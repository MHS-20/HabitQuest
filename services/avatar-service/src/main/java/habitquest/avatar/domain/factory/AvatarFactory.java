package habitquest.avatar.domain.factory;

import common.ddd.Id;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.items.EquippedItems;
import habitquest.avatar.domain.items.Inventory;
import habitquest.avatar.domain.stats.AvatarStats;

public class AvatarFactory {
  private final IdGenerator idGenerator;

  public AvatarFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Avatar create(Id<Avatar> id, String name) {
    return new Avatar(
        name,
        id,
        new Id<Inventory>(idGenerator.nextId()),
        new Id<EquippedItems>(idGenerator.nextId()),
        new Id<AvatarStats>(idGenerator.nextId()));
  }
}
