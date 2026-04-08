package habitquest.avatar.domain.items;

public record HealthPotion(BaseItem baseItem) implements Potion {
  public HealthPotion(String name, String description, int power) {
    this(new BaseItem(name, description, power));
  }
}
