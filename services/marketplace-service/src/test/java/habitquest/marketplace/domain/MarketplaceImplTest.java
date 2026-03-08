package habitquest.marketplace.domain;

import static org.assertj.core.api.Assertions.*;

import habitquest.marketplace.domain.items.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MarketplaceImplTest {

  private static final Money SWORD_PRICE = new Money(50);
  private static final Money SHIELD_PRICE = new Money(30);
  private static final Money HP_PRICE = new Money(10);
  private static final Money MP_PRICE = new Money(12);
  private static final Level LEVEL_1 = new Level(1);

  private Weapon sword;
  private Armor shield;
  private HealthPotion hpPotion;
  private ManaPotion mpPotion;
  private MarketplaceImpl marketplace;

  @BeforeEach
  void setUp() {
    sword = new Weapon("Iron Sword", "A basic sword", 10, SWORD_PRICE, LEVEL_1);
    shield = new Armor("Iron Shield", "A basic shield", 5, SHIELD_PRICE, LEVEL_1);
    hpPotion = new HealthPotion("HP Potion", "Restores 50 HP", 50, HP_PRICE, LEVEL_1);
    mpPotion = new ManaPotion("MP Potion", "Restores 30 MP", 30, MP_PRICE, LEVEL_1);

    marketplace =
        new MarketplaceImpl(
            "mp-1",
            new ArrayList<>(List.of(sword, shield, hpPotion, mpPotion)),
            new ArrayList<>(List.of(shield)),
            new ArrayList<>(List.of(sword)),
            new ArrayList<>(List.of(hpPotion, mpPotion)),
            new ArrayList<>(List.of(hpPotion)),
            new ArrayList<>(List.of(mpPotion)));
  }

  // ── Identity ─────────────────────────────────────────────────────────────────

  @Test
  void shouldReturnCorrectId() {
    assertThat(marketplace.getId()).isEqualTo("mp-1");
  }

  // ── Empty marketplace ─────────────────────────────────────────────────────────

  @Test
  void emptyMarketplaceShouldHaveNoItems() {
    MarketplaceImpl empty = new MarketplaceImpl("empty");
    assertThat(empty.getItems()).isEmpty();
    assertThat(empty.getArmors()).isEmpty();
    assertThat(empty.getWeapons()).isEmpty();
    assertThat(empty.getPotions()).isEmpty();
    assertThat(empty.getHealthPotions()).isEmpty();
    assertThat(empty.getManaPotions()).isEmpty();
  }

  // ── Queries ──────────────────────────────────────────────────────────────────

  @Nested
  class GetItems {

    @Test
    void shouldReturnAllItems() {
      assertThat(marketplace.getItems())
          .containsExactlyInAnyOrder(sword, shield, hpPotion, mpPotion);
    }

    @Test
    void shouldReturnArmors() {
      assertThat(marketplace.getArmors()).containsExactly(shield);
    }

    @Test
    void shouldReturnWeapons() {
      assertThat(marketplace.getWeapons()).containsExactly(sword);
    }

    @Test
    void shouldReturnAllPotions() {
      assertThat(marketplace.getPotions()).containsExactlyInAnyOrder(hpPotion, mpPotion);
    }

    @Test
    void shouldReturnHealthPotions() {
      assertThat(marketplace.getHealthPotions()).containsExactly(hpPotion);
    }

    @Test
    void shouldReturnManaPotions() {
      assertThat(marketplace.getManaPotions()).containsExactly(mpPotion);
    }
  }

  // ── getItem ──────────────────────────────────────────────────────────────────

  @Nested
  class GetItem {

    @Test
    void shouldFindExistingItemByName() {
      Optional<Item> result = marketplace.getItem("Iron Sword");
      assertThat(result).isPresent().contains(sword);
    }

    @Test
    void shouldReturnEmptyForUnknownItemName() {
      Optional<Item> result = marketplace.getItem("Legendary Axe");
      assertThat(result).isEmpty();
    }
  }

  // ── buyItem ──────────────────────────────────────────────────────────────────

  @Nested
  class BuyItem {

    @Test
    void shouldReturnItemPriceOnBuy() {
      Money price = marketplace.buyItem("Iron Sword");
      assertThat(price).isEqualTo(SWORD_PRICE);
    }

    @Test
    void shouldReturnCorrectPriceForEachItemType() {
      assertThat(marketplace.buyItem("Iron Shield")).isEqualTo(SHIELD_PRICE);
      assertThat(marketplace.buyItem("HP Potion")).isEqualTo(HP_PRICE);
      assertThat(marketplace.buyItem("MP Potion")).isEqualTo(MP_PRICE);
    }

    @Test
    void shouldThrowWhenBuyingNonExistentItem() {
      assertThatThrownBy(() -> marketplace.buyItem("Ghost Blade"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Ghost Blade");
    }
  }

  // ── sellItem ─────────────────────────────────────────────────────────────────

  @Nested
  class SellItem {

    @Test
    void shouldReturnItemPriceOnSell() {
      Money price = marketplace.sellItem("Iron Sword");
      assertThat(price).isEqualTo(SWORD_PRICE);
    }

    @Test
    void shouldReturnCorrectPriceForEachItemType() {
      assertThat(marketplace.sellItem("Iron Shield")).isEqualTo(SHIELD_PRICE);
      assertThat(marketplace.sellItem("HP Potion")).isEqualTo(HP_PRICE);
      assertThat(marketplace.sellItem("MP Potion")).isEqualTo(MP_PRICE);
    }

    @Test
    void shouldThrowWhenSellingNonExistentItem() {
      assertThatThrownBy(() -> marketplace.sellItem("Phantom Robe"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Phantom Robe");
    }
  }
}
