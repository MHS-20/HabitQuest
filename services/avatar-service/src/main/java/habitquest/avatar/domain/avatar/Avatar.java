package habitquest.avatar.domain.avatar;

import common.ddd.Aggregate;
import common.ddd.Id;
import habitquest.avatar.domain.items.Equipment;
import habitquest.avatar.domain.items.EquippedItems;
import habitquest.avatar.domain.items.Inventory;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.spells.Spell;
import habitquest.avatar.domain.stats.AvatarStats;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Avatar implements Aggregate<Id<Avatar>> {
  private final Id<Avatar> id;
  private String name;
  private Money money;
  private final Inventory inventory;
  private final EquippedItems equippedItems;
  private Level level;
  private AvatarHealth health;
  private AvatarMana mana;
  private final AvatarStats avatarStats;
  private final List<Spell> spells;
  private final List<Invite> pendingInvites;
  private Id<Guild> guildId;

  public Avatar(
      String name,
      Id<Avatar> id,
      Id<Inventory> invetoryId,
      Id<EquippedItems> equippedItemsId,
      Id<AvatarStats> avatarStatsId) {
    this.id = id;
    this.name = name;
    this.money = new Money(100);
    this.inventory = new Inventory(invetoryId);
    this.equippedItems = new EquippedItems(equippedItemsId);
    this.level = new Level(1, new Experience(0), new Experience(100));
    this.health = new AvatarHealth(new Health(100), new Health(100));
    this.mana = new AvatarMana(new Mana(50), new Mana(50));
    this.avatarStats = new AvatarStats(avatarStatsId, 10, 10, 10);
    this.spells = new ArrayList<>();
    this.pendingInvites = new ArrayList<>();
  }

  public Avatar(
      Id<Avatar> id,
      String name,
      Money money,
      Inventory inventory,
      EquippedItems equippedItems,
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
    this.level = level;
    this.health = health;
    this.mana = mana;
    this.avatarStats = avatarStats;
    this.spells = new ArrayList<>(spells);
    this.pendingInvites = new ArrayList<>();
  }

  @Override
  public Id<Avatar> getId() {
    return this.id;
  }

  public Id<Guild> getGuildId() {
    return guildId;
  }

  // --- Getters ---
  public String getName() {
    return name;
  }

  public List<Invite> getPendingInvites() {
    return Collections.unmodifiableList(pendingInvites);
  }

  public Money getMoney() {
    return money;
  }

  public List<Item> getInventory() {
    return Collections.unmodifiableList(inventory.getItems());
  }

  public List<Equipment> getEquippedItems() {
    return Collections.unmodifiableList(equippedItems.getItems());
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
    return Collections.unmodifiableList(spells);
  }

  // --- Metodi di dominio ---
  public void rename(String newName) {
    if (newName == null || newName.isBlank()) {
      throw new IllegalArgumentException("Name cannot be null or blank");
    }
    this.name = newName;
  }

  public void addPendingGuildInvite(Invite invite) {
    this.pendingInvites.add(invite);
  }

  public void acceptGuildInvite(Id<Invite> inviteId) {
    if (inviteId == null) {
      throw new IllegalArgumentException("Invite id cannot be null");
    }

    Invite invite =
        this.pendingInvites.stream()
            .filter(i -> i.inviteId().equals(inviteId))
            .findFirst()
            .orElse(null);

    if (invite == null) {
      throw new IllegalStateException("Invite not found in pending invites");
    }

    this.guildId = invite.guildId();
    this.pendingInvites.remove(invite);
  }

  public void gainExperience(Integer amount) {
    Level oldLevel = this.level;
    this.level = this.level.gainExperience(new Experience(amount));
    if (oldLevel.levelNumber() < this.level.levelNumber()) {
      onLevelUp();
    }
  }

  public boolean takeDamage(Damage damage) {
    this.health = this.health.damage(new Health(damage.value()));
    boolean died = this.health.isDead();
    if (died) {
      this.mana = this.mana.resetMana();
      this.health = this.health.resetHealth();
      this.level = this.level.resetExperience();
      this.money = this.money.subtract(new Money(100));
    }
    return died;
  }

  public void heal(Health amount) {
    this.health = this.health.heal(amount);
  }

  public void spendMana(Mana amount) {
    this.mana = this.mana.subtract(amount);
  }

  public void restoreMana(Mana amount) {
    this.mana = this.mana.add(amount);
  }

  private void onLevelUp() {
    this.health = this.health.increaseMax(new Health(10));
    this.mana = this.mana.increaseMax(new Mana(5));
    this.money = this.money.add(new Money(100));
  }

  // --- Money ---
  public void earnMoney(Money money) {
    if (money.amount() <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    this.money = this.money.add(money);
  }

  public void spendMoney(Money money) {
    if (money.amount() <= 0) {
      throw new IllegalArgumentException("Amount must be positive");
    }
    if (!this.money.isEnough(money)) {
      throw new IllegalStateException("Not enough money");
    }
    this.money = this.money.subtract(money);
  }

  // --- Inventory ---
  public void addItemToInventory(Item item) {
    this.inventory.addItem(item);
  }

  public void removeItemFromInventory(Item item) {
    this.inventory.removeItem(item);
  }

  public void equipItem(Equipment item) {
    if (!this.inventory.getItems().contains(item)) {
      throw new IllegalStateException("Cannot equip an item not in inventory");
    }
    this.equippedItems.equip(item);
    this.inventory.removeItem(item);
  }

  public void unequipItem(Equipment item) {
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
    spendMana(spell.getRequiredMana());
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
