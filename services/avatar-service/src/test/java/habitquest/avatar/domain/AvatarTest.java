package habitquest.avatar.domain;

import static org.assertj.core.api.Assertions.*;

import habitquest.avatar.domain.avatar.Avatar;
import habitquest.avatar.domain.avatar.Mana;
import habitquest.avatar.domain.items.Item;
import habitquest.avatar.domain.items.Weapon;
import habitquest.avatar.domain.spells.FireBall;
import habitquest.avatar.domain.spells.Spell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Avatar")
class AvatarTest {

  private Avatar avatar;

  @BeforeEach
  void setUp() {
    avatar = new Avatar("Hero", "id-1", "inv-1", "equip-1", "stats-1");
  }

  // ─── Identity ───────────────────────────────────────────────────────────────

  @Test
  @DisplayName("getId returns the id supplied at construction")
  void getId() {
    assertThat(avatar.getId()).isEqualTo("id-1");
  }

  @Test
  @DisplayName("getName returns the name supplied at construction")
  void getName() {
    assertThat(avatar.getName()).isEqualTo("Hero");
  }

  // ─── Rename ─────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("rename")
  class Rename {

    @Test
    @DisplayName("updates the avatar name")
    void renames() {
      avatar.rename("Warrior");
      assertThat(avatar.getName()).isEqualTo("Warrior");
    }

    @Test
    @DisplayName("throws on blank name")
    void blankName() {
      assertThatThrownBy(() -> avatar.rename("  ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("throws on null name")
    void nullName() {
      assertThatThrownBy(() -> avatar.rename(null)).isInstanceOf(IllegalArgumentException.class);
    }
  }

  // ─── Money ──────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("money")
  class MoneyOperations {

    @Test
    @DisplayName("starts at zero")
    void startsAtZero() {
      assertThat(avatar.getMoney().amount()).isZero();
    }

    @Test
    @DisplayName("earnMoney adds the amount")
    void earnMoney() {
      avatar.earnMoney(50);
      assertThat(avatar.getMoney().amount()).isEqualTo(50);
    }

    @Test
    @DisplayName("earnMoney throws on non-positive amount")
    void earnMoneyNonPositive() {
      assertThatThrownBy(() -> avatar.earnMoney(0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("spendMoney subtracts the amount when funds are sufficient")
    void spendMoney() {
      avatar.earnMoney(100);
      avatar.spendMoney(40);
      assertThat(avatar.getMoney().amount()).isEqualTo(60);
    }

    @Test
    @DisplayName("spendMoney throws when funds are insufficient")
    void spendMoneyInsufficient() {
      assertThatThrownBy(() -> avatar.spendMoney(1)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("spendMoney throws on non-positive amount")
    void spendMoneyNonPositive() {
      assertThatThrownBy(() -> avatar.spendMoney(0)).isInstanceOf(IllegalArgumentException.class);
    }
  }

  // ─── Combat ─────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("combat — health")
  class Combat {

    @Test
    @DisplayName("takeDamage reduces current health")
    void takeDamage() {
      int hpBefore = avatar.getHealth().current().value();
      avatar.takeDamage(20);
      assertThat(avatar.getHealth().current().value()).isEqualTo(hpBefore - 20);
    }

    @Test
    @DisplayName("heal increases current health")
    void heal() {
      avatar.takeDamage(50);
      avatar.heal(30);
      assertThat(avatar.getHealth().current().value()).isEqualTo(80);
    }

    @Test
    @DisplayName("heal cannot exceed max health")
    void healCannotExceedMax() {
      avatar.heal(999);
      assertThat(avatar.getHealth().current().value()).isEqualTo(avatar.getHealth().max().value());
    }

    @Nested
    @DisplayName("death on lethal damage")
    class Death {

      @Test
      @DisplayName("current health resets to max after lethal damage")
      void healthResets() {
        avatar.earnMoney(200); // ensure money can be subtracted
        avatar.takeDamage(9999);
        assertThat(avatar.getHealth().current().value())
            .isEqualTo(avatar.getHealth().max().value());
      }

      @Test
      @DisplayName("mana resets to max after lethal damage")
      void manaResets() {
        avatar.earnMoney(200);
        avatar.spendMana(20);
        avatar.takeDamage(9999);
        assertThat(avatar.getMana().amount().value()).isEqualTo(avatar.getMana().max().value());
      }

      @Test
      @DisplayName("experience resets to zero after lethal damage")
      void experienceResets() {
        avatar.earnMoney(200);
        avatar.gainExperience(50);
        avatar.takeDamage(9999);
        assertThat(avatar.getLevel().currentExperience().amount()).isZero();
      }

      @Test
      @DisplayName("100 coins are deducted on death")
      void moneyDeducted() {
        avatar.earnMoney(200);
        avatar.takeDamage(9999);
        // 200 earned initially − 100 penalty
        assertThat(avatar.getMoney().amount()).isEqualTo(100);
      }
    }
  }

  // ─── Mana ───────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("mana")
  class ManaOperations {

    @Test
    @DisplayName("spendMana reduces current mana")
    void spendMana() {
      int manaBefore = avatar.getMana().amount().value();
      avatar.spendMana(10);
      assertThat(avatar.getMana().amount().value()).isEqualTo(manaBefore - 10);
    }

    @Test
    @DisplayName("spendMana throws when mana is insufficient")
    void spendManaInsufficient() {
      assertThatThrownBy(() -> avatar.spendMana(9999)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("restoreMana increases current mana up to max")
    void restoreMana() {
      avatar.spendMana(20);
      avatar.restoreMana(10);
      assertThat(avatar.getMana().amount().value()).isEqualTo(40);
    }
  }

  // ─── Experience & levelling ──────────────────────────────────────────────────

  @Nested
  @DisplayName("experience and levelling")
  class ExperienceAndLevelling {

    @Test
    @DisplayName("gainExperience increases current XP without levelling up")
    void gainsXp() {
      avatar.gainExperience(50);
      assertThat(avatar.getLevel().currentExperience().amount()).isEqualTo(50);
      assertThat(avatar.getLevel().levelNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("gainExperience triggers level-up when threshold is reached")
    void levelUp() {
      avatar.gainExperience(100);
      assertThat(avatar.getLevel().levelNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("levelling up resets current XP to zero")
    void xpResetOnLevelUp() {
      avatar.gainExperience(100);
      assertThat(avatar.getLevel().currentExperience().amount()).isZero();
    }

    @Test
    @DisplayName("levelling up grants 10 extra max HP")
    void hpIncreaseOnLevelUp() {
      int maxHpBefore = avatar.getHealth().max().value();
      avatar.gainExperience(100);
      assertThat(avatar.getHealth().max().value()).isEqualTo(maxHpBefore + 10);
    }

    @Test
    @DisplayName("levelling up grants 5 extra max mana")
    void manaIncreaseOnLevelUp() {
      int maxManaBefore = avatar.getMana().max().value();
      avatar.gainExperience(100);
      assertThat(avatar.getMana().max().value()).isEqualTo(maxManaBefore + 5);
    }

    @Test
    @DisplayName("levelling up grants 100 coins")
    void moneyGrantedOnLevelUp() {
      avatar.gainExperience(100);
      assertThat(avatar.getMoney().amount()).isEqualTo(100);
    }
  }

  // ─── Inventory ──────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("inventory")
  class InventoryOperations {

    private final Item sword = new Weapon("Sword", "A sharp blade", 15);

    @Test
    @DisplayName("addItemToInventory stores the item")
    void addItem() {
      avatar.addItemToInventory(sword);
      assertThat(avatar.getInventory().getItems()).contains(sword);
    }

    @Test
    @DisplayName("removeItemFromInventory removes the item")
    void removeItem() {
      avatar.addItemToInventory(sword);
      avatar.removeItemFromInventory(sword);
      assertThat(avatar.getInventory().getItems()).doesNotContain(sword);
    }

    @Test
    @DisplayName("removeItemFromInventory throws when item is absent")
    void removeAbsentItem() {
      assertThatThrownBy(() -> avatar.removeItemFromInventory(sword))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("equipItem moves item from inventory to equipped slots")
    void equipItem() {
      avatar.addItemToInventory(sword);
      avatar.equipItem(sword);
      assertThat(avatar.getEquippedItems().getItems()).contains(sword);
      assertThat(avatar.getInventory().getItems()).doesNotContain(sword);
    }

    @Test
    @DisplayName("equipItem throws when item is not in inventory")
    void equipAbsentItem() {
      assertThatThrownBy(() -> avatar.equipItem(sword)).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("unequipItem moves item from equipped back to inventory")
    void unequipItem() {
      avatar.addItemToInventory(sword);
      avatar.equipItem(sword);
      avatar.unequipItem(sword);
      assertThat(avatar.getInventory().getItems()).contains(sword);
      assertThat(avatar.getEquippedItems().getItems()).doesNotContain(sword);
    }
  }

  // ─── Spells ─────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("spells")
  class SpellOperations {

    private final Spell fireball = new FireBall("Fireball", "Burns things", 10, new Mana(5));

    @Test
    @DisplayName("learnSpell adds spell to the known list")
    void learnSpell() {
      avatar.learnSpell(fireball);
      assertThat(avatar.getSpells()).contains(fireball);
    }

    @Test
    @DisplayName("learnSpell throws when spell is already known")
    void learnDuplicateSpell() {
      avatar.learnSpell(fireball);
      assertThatThrownBy(() -> avatar.learnSpell(fireball))
          .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("castSpell spends the required mana")
    void castSpell() {
      avatar.learnSpell(fireball);
      int manaBefore = avatar.getMana().amount().value();
      avatar.castSpell(fireball);
      assertThat(avatar.getMana().amount().value())
          .isEqualTo(manaBefore - fireball.requiredMana().value());
    }

    @Test
    @DisplayName("castSpell throws when spell is not known")
    void castUnknownSpell() {
      assertThatThrownBy(() -> avatar.castSpell(fireball))
          .isInstanceOf(IllegalStateException.class);
    }
  }

  // ─── Stats ───────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("stats delegation")
  class StatsDelegation {

    @Test
    @DisplayName("incrementStrength increments the strength stat")
    void strength() {
      int before = avatar.getAvatarStats().getStrength().value();
      avatar.incrementStrength();
      assertThat(avatar.getAvatarStats().getStrength().value()).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("incrementDefense increments the defense stat")
    void defense() {
      int before = avatar.getAvatarStats().getDefense().value();
      avatar.incrementDefense();
      assertThat(avatar.getAvatarStats().getDefense().value()).isEqualTo(before + 1);
    }

    @Test
    @DisplayName("incrementIntelligence increments the intelligence stat")
    void intelligence() {
      int before = avatar.getAvatarStats().getIntelligence().value();
      avatar.incrementIntelligence();
      assertThat(avatar.getAvatarStats().getIntelligence().value()).isEqualTo(before + 1);
    }
  }
}
