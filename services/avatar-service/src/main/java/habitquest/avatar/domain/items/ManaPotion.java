package habitquest.avatar.domain.items;

public record ManaPotion(BaseItem baseItem) implements Potion {
  public ManaPotion(String name, String description, int power) {
    this(new BaseItem(name, description, power));
  }
}
