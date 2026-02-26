package habitquest.avatar.domain.items;

public record Weapon(BaseItem baseItem, Integer attackPower) implements Item {
  public Weapon(String name, String description, Integer attackPower) {
    this(new BaseItem(name, description), attackPower);
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
