package habitquest.avatar;

import common.ddd.Id;
import habitquest.avatar.domain.avatar.Avatar;
import habitquest.avatar.domain.avatar.AvatarHealth;
import habitquest.avatar.domain.avatar.AvatarMana;
import habitquest.avatar.domain.avatar.Experience;
import habitquest.avatar.domain.avatar.Health;
import habitquest.avatar.domain.avatar.Level;
import habitquest.avatar.domain.avatar.Mana;
import habitquest.avatar.domain.avatar.Money;
import habitquest.avatar.domain.items.Armor;
import habitquest.avatar.domain.items.EquippedItems;
import habitquest.avatar.domain.items.Inventory;
import habitquest.avatar.domain.items.Weapon;
import habitquest.avatar.domain.spells.Spell;
import habitquest.avatar.domain.stats.AvatarStats;
import habitquest.avatar.infrastructure.dto.AvatarResponsesDto.*;
import java.util.ArrayList;
import java.util.List;

public final class AvatarFixtures {
  public static final String AVATAR_1 = "avatar-1";
  public static final String AVATAR_NAME = "Hero";
  public static final String UNKNOWN_AVATAR = "ghost-99";

  public static final String INVENTORY_1 = "inv-1";
  public static final String EQUIP_1 = "equip-1";
  public static final String STATS_1 = "stats-1";

  public static final int DEFAULT_HEALTH = 100;
  public static final int DEFAULT_MANA = 50;
  public static final int DEFAULT_STRENGTH = 10;
  public static final int DEFAULT_DEFENSE = 10;
  public static final int DEFAULT_INTELLIGENCE = 10;
  public static final int DEFAULT_XP_TO_NEXT = 100;

  public static final String FIELD_OCCURRED_ON = "occurredOn";
  public static final String FIELD_NEW_LEVEL = "newLevel";
  public static final String FIELD_AVATAR_ID = "avatarId";
  public static final String FIELD_STAT_TYPE = "statType";
  public static final String FIELD_NEW_VALUE = "newValue";
  public static final String FIELD_SPELL_NAME = "spellName";
  public static final String FIELD_DESCRIPTION = "description";

  public static final Id<Avatar> AVATAR_ID = new Id<>(AVATAR_1);
  public static final Id<Avatar> UNKNOWN_ID = new Id<>(UNKNOWN_AVATAR);
  public static final Id<Inventory> INVENTORY_ID = new Id<>(INVENTORY_1);
  public static final Id<EquippedItems> EQUIPPED_ID = new Id<>(EQUIP_1);
  public static final Id<AvatarStats> STATS_ID = new Id<>(STATS_1);

  public static final Weapon SWORD = new Weapon("Iron Sword", "A basic sword", 15);
  public static final Armor SHIELD = new Armor("Iron Shield", "A basic shield", 5);

  // Avatar factories
  public static Avatar mutableAvatar() {
    return new Avatar(
        new Id<>(AVATAR_1),
        AVATAR_NAME,
        new Money(0),
        new Inventory(new Id<>(INVENTORY_1)),
        new EquippedItems(new Id<>(EQUIP_1)),
        new Level(1, new Experience(0), new Experience(DEFAULT_XP_TO_NEXT)),
        new AvatarHealth(new Health(DEFAULT_HEALTH), new Health(DEFAULT_HEALTH)),
        new AvatarMana(new Mana(DEFAULT_MANA), new Mana(DEFAULT_MANA)),
        new AvatarStats(new Id<>(STATS_1), DEFAULT_STRENGTH, DEFAULT_DEFENSE, DEFAULT_INTELLIGENCE),
        new ArrayList<>());
  }

  public static Avatar readOnlyAvatar() {
    return new Avatar(
        new Id<>(AVATAR_1),
        AVATAR_NAME,
        new Money(0),
        new Inventory(new Id<>(INVENTORY_1)),
        new EquippedItems(new Id<>(EQUIP_1)),
        new Level(1, new Experience(0), new Experience(DEFAULT_XP_TO_NEXT)),
        new AvatarHealth(new Health(DEFAULT_HEALTH), new Health(DEFAULT_HEALTH)),
        new AvatarMana(new Mana(DEFAULT_MANA), new Mana(DEFAULT_MANA)),
        new AvatarStats(new Id<>(STATS_1), DEFAULT_STRENGTH, DEFAULT_DEFENSE, DEFAULT_INTELLIGENCE),
        List.of());
  }

  public static Avatar avatarAtLevel(int level, int xpToNext) {
    return new Avatar(
        new Id<>(AVATAR_1),
        AVATAR_NAME,
        new Money(0),
        new Inventory(new Id<>(INVENTORY_1)),
        new EquippedItems(new Id<>(EQUIP_1)),
        new Level(level, new Experience(0), new Experience(xpToNext)),
        new AvatarHealth(new Health(DEFAULT_HEALTH), new Health(DEFAULT_HEALTH)),
        new AvatarMana(new Mana(DEFAULT_MANA), new Mana(DEFAULT_MANA)),
        new AvatarStats(new Id<>(STATS_1), DEFAULT_STRENGTH, DEFAULT_DEFENSE, DEFAULT_INTELLIGENCE),
        new ArrayList<>());
  }

  public static Avatar avatarAtLevel4() {
    return avatarAtLevel(4, 100);
  }

  public static AvatarResponse avatarResponse() {
    return new AvatarResponse(
        AVATAR_1,
        AVATAR_NAME,
        new MoneyResponse(0),
        new LevelResponse(1, 0, DEFAULT_XP_TO_NEXT),
        new HealthResponse(DEFAULT_HEALTH, DEFAULT_HEALTH),
        new ManaResponse(DEFAULT_MANA, DEFAULT_MANA),
        new StatsResponse(DEFAULT_STRENGTH, DEFAULT_DEFENSE, DEFAULT_INTELLIGENCE),
        new InventoryResponse(List.of()),
        new EquippedItemsResponse(AVATAR_1, List.of()),
        new ArrayList<>(List.of(Spell.FIREBALL.getName())));
  }

  // Prevent instantiation
  private AvatarFixtures() {
    throw new UnsupportedOperationException("utility class");
  }
}
