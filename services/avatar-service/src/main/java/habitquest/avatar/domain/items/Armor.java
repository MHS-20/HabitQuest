package habitquest.avatar.domain.items;

public record Armor(BaseItem baseItem) implements Item {
  public Armor(String name, String description, int power) {
    this(new BaseItem(name, description, power));
  }
}
