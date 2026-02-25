package habitquest.avatar_service.domain.items;

public record HealthPotion(BaseItem baseItem, Integer healingPower) implements Item {
  public HealthPotion(String name, String description, Integer healingPower) {
    this(new BaseItem(name, description), healingPower);
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
