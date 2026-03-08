package habitquest.marketplace.domain;

import static org.assertj.core.api.Assertions.*;

import habitquest.marketplace.domain.items.*;
import org.junit.jupiter.api.Test;

class ItemTest {

  // ── Helpers ─────────────────────────────────────────────────────────────────

  private static final Money TEN_GOLD = new Money(10);
  private static final Money ZERO_GOLD = new Money(0);
  private static final Level LEVEL_1 = new Level(1);
  private static final Level LEVEL_5 = new Level(5);
  private static final String DESC = "desc";

  // ── Level ────────────────────────────────────────────────────────────────────

  @Test
  void levelShouldRejectZero() {
    assertThatThrownBy(() -> new Level(0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("at least 1");
  }

  @Test
  void levelShouldRejectNegative() {
    assertThatThrownBy(() -> new Level(-3)).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void levelShouldAcceptOne() {
    assertThat(new Level(1).levelNumber()).isEqualTo(1);
  }

  // ── BaseItem ─────────────────────────────────────────────────────────────────

  @Test
  void baseItemShouldRejectBlankName() {
    assertThatThrownBy(() -> new BaseItem("  ", DESC, TEN_GOLD, LEVEL_1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("name");
  }

  @Test
  void baseItemShouldRejectBlankDescription() {
    assertThatThrownBy(() -> new BaseItem("Sword", "  ", TEN_GOLD, LEVEL_1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("description");
  }

  @Test
  void baseItemShouldRejectNullName() {
    assertThatThrownBy(() -> new BaseItem(null, DESC, TEN_GOLD, LEVEL_1))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void baseItemCanBuyWhenPlayerLevelMeetsRequirement() {
    BaseItem item = new BaseItem("Item", DESC, TEN_GOLD, LEVEL_5);
    assertThat(item.canBuy(LEVEL_5)).isTrue();
    assertThat(item.canBuy(new Level(10))).isTrue();
  }

  @Test
  void baseItemCannotBuyWhenPlayerLevelIsTooLow() {
    BaseItem item = new BaseItem("Item", DESC, TEN_GOLD, LEVEL_5);
    assertThat(item.canBuy(LEVEL_1)).isFalse();
    assertThat(item.canBuy(new Level(4))).isFalse();
  }

  // ── Weapon ───────────────────────────────────────────────────────────────────

  @Test
  void weaponShouldExposeCorrectFields() {
    Weapon sword = new Weapon("Iron Sword", "A sturdy blade", 15, TEN_GOLD, LEVEL_1);

    assertThat(sword.name()).isEqualTo("Iron Sword");
    assertThat(sword.description()).isEqualTo("A sturdy blade");
    assertThat(sword.attackPower()).isEqualTo(15);
    assertThat(sword.price()).isEqualTo(TEN_GOLD);
    assertThat(sword.requiredLevel()).isEqualTo(LEVEL_1);
  }

  @Test
  void weaponShouldRejectNegativeAttackPower() {
    assertThatThrownBy(() -> new Weapon("Sword", DESC, -1, TEN_GOLD, LEVEL_1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Attack power");
  }

  @Test
  void weaponShouldAllowZeroAttackPower() {
    assertThatNoException()
        .isThrownBy(() -> new Weapon("Training Sword", "for beginners", 0, ZERO_GOLD, LEVEL_1));
  }

  // ── Armor ────────────────────────────────────────────────────────────────────

  @Test
  void armorShouldExposeCorrectFields() {
    Armor shield = new Armor("Iron Shield", "Blocks hits", 20, TEN_GOLD, LEVEL_1);

    assertThat(shield.name()).isEqualTo("Iron Shield");
    assertThat(shield.defensePower()).isEqualTo(20);
    assertThat(shield.price()).isEqualTo(TEN_GOLD);
  }

  @Test
  void armorShouldRejectNegativeDefensePower() {
    assertThatThrownBy(() -> new Armor("Shield", DESC, -5, TEN_GOLD, LEVEL_1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Defense power");
  }

  // ── HealthPotion ─────────────────────────────────────────────────────────────

  @Test
  void healthPotionShouldExposeCorrectFields() {
    HealthPotion potion =
        new HealthPotion("Minor HP Potion", "Restores 50 HP", 50, TEN_GOLD, LEVEL_1);

    assertThat(potion.name()).isEqualTo("Minor HP Potion");
    assertThat(potion.healingPower()).isEqualTo(50);
    assertThat(potion.price()).isEqualTo(TEN_GOLD);
  }

  @Test
  void healthPotionShouldRejectNegativeHealingPower() {
    assertThatThrownBy(() -> new HealthPotion("Potion", DESC, -10, TEN_GOLD, LEVEL_1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Healing power");
  }

  @Test
  void healthPotionShouldImplementPotionInterface() {
    HealthPotion potion = new HealthPotion("HP Potion", DESC, 10, TEN_GOLD, LEVEL_1);
    assertThat(potion).isInstanceOf(Potion.class);
  }

  // ── ManaPotion ───────────────────────────────────────────────────────────────

  @Test
  void manaPotionShouldExposeCorrectFields() {
    ManaPotion potion = new ManaPotion("Minor MP Potion", "Restores 30 MP", 30, TEN_GOLD, LEVEL_1);

    assertThat(potion.name()).isEqualTo("Minor MP Potion");
    assertThat(potion.restoringPower()).isEqualTo(30);
    assertThat(potion.price()).isEqualTo(TEN_GOLD);
  }

  @Test
  void manaPotionShouldRejectNegativeRestoringPower() {
    assertThatThrownBy(() -> new ManaPotion("Potion", DESC, -1, TEN_GOLD, LEVEL_1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Mana power");
  }

  @Test
  void manaPotionShouldImplementPotionInterface() {
    ManaPotion potion = new ManaPotion("MP Potion", DESC, 10, TEN_GOLD, LEVEL_1);
    assertThat(potion).isInstanceOf(Potion.class);
  }
}
