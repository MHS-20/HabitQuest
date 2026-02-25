package habitquest.avatar_service.domain.items;

import java.util.Objects;

public record Armor(BaseItem baseItem, Integer defensePower) implements Item {
  public Armor {
    Objects.requireNonNull(baseItem);
    if (defensePower < 0) {
      throw new IllegalArgumentException("Defense power cannot be negative");
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
    return defensePower;
  }
}
