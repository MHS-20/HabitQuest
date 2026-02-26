package habitquest.avatar.domain.items;

public record Armor(BaseItem baseItem, Integer defensePower) implements Item {
  public Armor(String name, String description, Integer defensePower) {
    this(new BaseItem(name, description), defensePower);
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
