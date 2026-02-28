package habitquest.avatar.domain.avatar;

import common.ddd.Aggregate;
import habitquest.avatar.domain.items.EquippedItems;
import habitquest.avatar.domain.items.Inventory;
import habitquest.avatar.domain.stats.AvatarStats;

public class Avatar implements Aggregate<String> {
  private final String id;
  private String name;
  private Money money;
  private Inventory inventory;
  private EquippedItems equippedItems;
  private Experience experience;
  private Level level;
  private Health health;
  private Mana mana;
  private AvatarStats avatarStats;

  public Avatar(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return this.id;
  }
}
