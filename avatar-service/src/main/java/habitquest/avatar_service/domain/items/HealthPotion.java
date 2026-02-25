package habitquest.avatar_service.domain.items;

import java.util.Objects;

public record HealthPotion(BaseItem baseItem, Integer healingPower) implements Item {
  public HealthPotion {
    Objects.requireNonNull(baseItem);
    if (healingPower < 0) {
      throw new IllegalArgumentException("Healing power cannot be negative");
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
    return healingPower;
  }
}
