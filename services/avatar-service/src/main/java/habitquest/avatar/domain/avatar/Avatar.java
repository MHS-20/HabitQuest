package habitquest.avatar.domain.avatar;

import common.ddd.Aggregate;
import habitquest.avatar.domain.items.EquippedItems;
import habitquest.avatar.domain.items.Inventory;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.spells.Spell;
import habitquest.avatar.domain.stats.AvatarStats;
import java.util.ArrayList;
import java.util.List;

public class Avatar implements Aggregate<String> {
  private final String id;
  private String name;
  private Money money;
  private Inventory inventory;
  private EquippedItems equippedItems;
  // private Experience experience;
  private Level level;
  private AvatarHealth health;
  private AvatarMana mana;
  private AvatarStats avatarStats;
  private List<Spell> spells;

  public Avatar(
      String name, String id, String invetoryId, String equippedItemsId, String avatarStatsId) {
    this.id = id;
    this.name = name;
    this.money = new Money(0);
    this.inventory = new Inventory(invetoryId);
    this.equippedItems = new EquippedItems(equippedItemsId);
    // this.experience = new Experience(0);
    this.level = new Level(1, new Experience(0), new Experience(100));
    this.health = new AvatarHealth(new Health(100), new Health(100));
    this.mana = new AvatarMana(new Mana(50), new Mana(50));
    this.avatarStats = new AvatarStats(avatarStatsId, 10, 10, 10);
    this.spells = new ArrayList<>();
  }

  public Avatar(
      String id,
      String name,
      Money money,
      Inventory inventory,
      EquippedItems equippedItems,
      Experience experience,
      Level level,
      AvatarHealth health,
      AvatarMana mana,
      AvatarStats avatarStats,
      List<Spell> spells) {
    this.id = id;
    this.name = name;
    this.money = money;
    this.inventory = inventory;
    this.equippedItems = equippedItems;
    // this.experience = experience;
    this.level = level;
    this.health = health;
    this.mana = mana;
    this.avatarStats = avatarStats;
    this.spells = spells;
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

  public Level getLevel() {
    return level;
  }

  public AvatarHealth getHealth() {
    return health;
  }

  public AvatarMana getMana() {
    return mana;
  }

  public AvatarStats getAvatarStats() {
    return avatarStats;
  }

  public List<Spell> getSpells() {
    return spells;
  }

  // --- Metodi di dominio ---
  public void rename(String newName) {
    if (newName == null || newName.isBlank()) {
      throw new IllegalArgumentException("Name cannot be null or blank");
    }
    this.name = newName;
  }

  public void gainExperience(Integer amount) {
    Level oldLevel = this.level;
    this.level = this.level.gainExperience(new Experience(amount));
    if (oldLevel.levelNumber() < this.level.levelNumber()) {
      onLevelUp();
    }
  }

  public void takeDamage(Integer amount) {
    this.health = this.health.damage(new Health(amount));
    if (this.health.isDead()) {
      this.mana = this.mana.resetMana();
      this.health = this.health.resetHealth();
      this.level = this.level.resetExperience();
      this.money = this.money.subtract(new Money(100));
    }
  }

  public void heal(Integer amount) {
    this.health = this.health.heal(new Health(amount));
  }

  public void spendMana(Integer amount) {
    this.mana = this.mana.subtract(new Mana(amount));
  }

  public void restoreMana(Integer amount) {
    this.mana = this.mana.add(new Mana(amount));
  }

  private void onLevelUp() {
    this.health = this.health.increaseMax(new Health(10));
    this.mana = this.mana.increaseMax(new Mana(5));
    this.money = this.money.add(new Money(100));
  }

  // --- Money ---
  public void earnMoney(Integer amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    this.money = this.money.add(new Money(amount));
  }

  public void spendMoney(Integer amount) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    if (this.money.isEnough(new Money(amount))) {
      throw new IllegalStateException("Not enough money");
    }
    this.money = this.money.subtract(new Money(amount));
  }

  // --- Inventory ---
  public void addItemToInventory(Item item) {
    this.inventory.addItem(item);
  }

  public void removeItemFromInventory(Item item) {
    this.inventory.removeItem(item);
  }

  public void equipItem(Item item) {
    if (!this.inventory.getItems().contains(item)) {
      throw new IllegalStateException("Cannot equip an item not in inventory");
    }
    this.equippedItems.equip(item);
    this.inventory.removeItem(item);
  }

  public void unequipItem(Item item) {
    this.equippedItems.unequip(item);
    this.inventory.addItem(item);
  }

  // --- Spells ---
  public void learnSpell(Spell spell) {
    if (this.spells.contains(spell)) {
      throw new IllegalStateException("Spell already known: " + spell.name());
    }
    this.spells.add(spell);
  }

  public void castSpell(Spell spell) {
    if (!this.spells.contains(spell)) {
      throw new IllegalStateException("Spell not known: " + spell.name());
    }
    spendMana(spell.requiredMana().value());
  }

  // --- Stats ---
  public void incrementStrength() {
    this.avatarStats.incrementStrength();
  }

  public void incrementDefense() {
    this.avatarStats.incrementDefense();
  }

  public void incrementIntelligence() {
    this.avatarStats.incrementIntelligence();
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
