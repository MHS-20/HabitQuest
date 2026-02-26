package habitquest.avatar.domain.items;

public record ManaPotion(BaseItem baseItem, Integer restoringPower) implements Item, Potion {
  public ManaPotion(String name, String description, Integer restoringPower) {
    this(new BaseItem(name, description), restoringPower);
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
