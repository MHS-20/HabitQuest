package habitquest.avatar_service.domain.items;

import java.util.Objects;

public record ManaPotion(BaseItem baseItem, Integer restoringPower) implements Item {
  public ManaPotion {
    Objects.requireNonNull(baseItem);
    if (restoringPower < 0) {
      throw new IllegalArgumentException("Mana power cannot be negative");
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
    return restoringPower;
  }
}
