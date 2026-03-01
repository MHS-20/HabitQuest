package habitquest.avatar.domain.avatar;

import common.ddd.Aggregate;
import habitquest.avatar.domain.items.EquippedItems;
import habitquest.avatar.domain.items.Inventory;
import habitquest.avatar.domain.stats.AvatarStats;

public class Avatar implements Aggregate<String> {
  private final String id;
  private String name;
  private Money money;
  private Inventory inventory;
  private EquippedItems equippedItems;
  private Experience experience;
  private Level level;
  private Health health;
  private Mana mana;
  private AvatarStats avatarStats;

  public Avatar(
      String name, String id, String invetoryId, String equippedItemsId, String avatarStatsId) {
    this.id = id;
    this.name = name;
    this.money = new Money(0);
    this.inventory = new Inventory(invetoryId);
    this.equippedItems = new EquippedItems(equippedItemsId);
    this.experience = new Experience(0);
    this.level = new Level(1, new Experience(100));
    this.health = new Health(100, 100);
    this.mana = new Mana(50, 50);
    this.avatarStats = new AvatarStats(avatarStatsId, 10, 10, 10);
  }

  public Avatar(
      String id,
      String name,
      Money money,
      Inventory inventory,
      EquippedItems equippedItems,
      Experience experience,
      Level level,
      Health health,
      Mana mana,
      AvatarStats avatarStats) {
    this.id = id;
    this.name = name;
    this.money = money;
    this.inventory = inventory;
    this.equippedItems = equippedItems;
    this.experience = experience;
    this.level = level;
    this.health = health;
    this.mana = mana;
    this.avatarStats = avatarStats;
  }

  @Override
  public String getId() {
    return this.id;
  }

  // --- Getters ---
  public String getName() {
    return name;
  }

  public Money getMoney() {
    return money;
  }

  public Inventory getInventory() {
    return inventory;
  }

  public EquippedItems getEquippedItems() {
    return equippedItems;
  }

  public Experience getExperience() {
    return experience;
  }

  public Level getLevel() {
    return level;
  }

  public Health getHealth() {
    return health;
  }

  public Mana getMana() {
    return mana;
  }

  public AvatarStats getAvatarStats() {
    return avatarStats;
  }

  // --- Setters ---
  public void setName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Name cannot be null or blank");
    }
    this.name = name;
  }

  public void setMoney(Money money) {
    this.money = money;
  }

  public void setInventory(Inventory inventory) {
    this.inventory = inventory;
  }

  public void setEquippedItems(EquippedItems equippedItems) {
    this.equippedItems = equippedItems;
  }

  public void setExperience(Experience experience) {
    this.experience = experience;
  }

  public void setLevel(Level level) {
    this.level = level;
  }

  public void setHealth(Health health) {
    this.health = health;
  }

  public void setMana(Mana mana) {
    this.mana = mana;
  }

  public void setAvatarStats(AvatarStats avatarStats) {
    this.avatarStats = avatarStats;
  }

  // --- Metodi di dominio ---
  public void gainExperience(Integer amount) {
    this.experience = this.experience.add(new Experience(amount));
  }

  public void takeDamage(Integer amount) {
    this.health = this.health.damage(new Health(amount, this.health.max()));
  }

  public void heal(Integer amount) {
    this.health = this.health.heal(new Health(amount, this.health.max()));
  }

  public void spendMana(Integer amount) {
    this.mana = this.mana.subtract(new Mana(amount, this.mana.max()));
  }

  public void restoreMana(Integer amount) {
    this.mana = this.mana.resetMana();
  }

  public boolean isAlive() {
    return this.health != null && !this.health.isDead();
  }

  @Override
  public String toString() {
    return "Avatar{"
        + "id='"
        + id
        + '\''
        + ", name='"
        + name
        + '\''
        + ", level="
        + level
        + ", health="
        + health
        + ", mana="
        + mana
        + '}';
  }
}
