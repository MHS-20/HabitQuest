package habitquest.avatar_service.domain;

import common.ddd.Entity;

public class Avatar implements Entity<String> {
  private final String id;
  private Money money;
  private Inventory inventory;
  private EquippedItems equippedItems;
  private Experience experience;
  private Level level;
  private Health health;
  private Mana mana;
  private PlayerStats playerStats;

  public Avatar(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return this.id;
  }
}
