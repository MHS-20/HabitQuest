package habitquest.avatar_service.domain.items;

import java.util.Objects;

public record Weapon(BaseItem baseItem, Integer attackPower) implements Item {
  public Weapon {
    Objects.requireNonNull(baseItem);
    if (attackPower < 0) {
      throw new IllegalArgumentException("Attack power cannot be negative");
    }
  }

  @Override
  public String name() {
    return baseItem.name();
  }

  @Override
  public String description() {
    return baseItem.description();
  }

  public Integer getValue() {
    return attackPower;
  }
}
