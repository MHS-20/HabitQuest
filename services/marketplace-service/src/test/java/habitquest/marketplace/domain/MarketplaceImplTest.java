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

  private static final String MARKETPLACE_ID = "mp-1";
  private static final String SWORD_NAME = "Iron Sword";
  private static final String SHIELD_NAME = "Iron Shield";
  private static final String HP_POTION_NAME = "HP Potion";
  private static final String MP_POTION_NAME = "MP Potion";
  private static final String SWORD_DESC = "A basic sword";
  private static final String SHIELD_DESC = "A basic shield";
  private static final String HP_POTION_DESC = "Restores 50 HP";
  private static final String MP_POTION_DESC = "Restores 30 MP";
  private static final String UNKNOWN_ITEM_NAME = "Legendary Axe";
  private static final String UNKNOWN_BUY_ITEM_NAME = "Ghost Blade";
  private static final String UNKNOWN_SELL_ITEM_NAME = "Phantom Robe";
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
    sword = new Weapon(SWORD_NAME, SWORD_DESC, 10, SWORD_PRICE, LEVEL_1);
    shield = new Armor(SHIELD_NAME, SHIELD_DESC, 5, SHIELD_PRICE, LEVEL_1);
    hpPotion = new HealthPotion(HP_POTION_NAME, HP_POTION_DESC, 50, HP_PRICE, LEVEL_1);
    mpPotion = new ManaPotion(MP_POTION_NAME, MP_POTION_DESC, 30, MP_PRICE, LEVEL_1);

    marketplace =
        new MarketplaceImpl(
            MARKETPLACE_ID, new ArrayList<>(List.of(sword, shield, hpPotion, mpPotion)));
  }

  // ── Identity ─────────────────────────────────────────────────────────────────

  @Test
  void shouldReturnCorrectId() {
    assertThat(marketplace.getId()).isEqualTo(MARKETPLACE_ID);
  }

  // ── Empty marketplace ─────────────────────────────────────────────────────────

  @Test
  void emptyMarketplaceShouldHaveNoItems() {
    MarketplaceImpl empty = new MarketplaceImpl("empty");
    assertThat(empty.getItems(ItemType.ALL)).isEmpty();
  }

  // ── Queries ──────────────────────────────────────────────────────────────────

  @Nested
  class GetItems {

    @Test
    void shouldReturnAllItems() {
      assertThat(marketplace.getItems(ItemType.ALL))
          .containsExactlyInAnyOrder(sword, shield, hpPotion, mpPotion);
    }

    @Test
    void shouldReturnArmors() {
      assertThat(marketplace.getItems(ItemType.ARMOR)).containsExactly(shield);
    }

    @Test
    void shouldReturnWeapons() {
      assertThat(marketplace.getItems(ItemType.WEAPON)).containsExactly(sword);
    }

    @Test
    void shouldReturnAllPotions() {
      assertThat(marketplace.getItems(ItemType.POTION))
          .containsExactlyInAnyOrder(hpPotion, mpPotion);
    }

    @Test
    void shouldReturnHealthPotions() {
      assertThat(marketplace.getItems(ItemType.HEALTH_POTION)).containsExactly(hpPotion);
    }

    @Test
    void shouldReturnManaPotions() {
      assertThat(marketplace.getItems(ItemType.MANA_POTION)).containsExactly(mpPotion);
    }
  }

  // ── getItem ──────────────────────────────────────────────────────────────────
  @Nested
  class GetItem {

    @Test
    void shouldFindExistingItemByName() {
      Optional<Item> result = marketplace.getItem(SWORD_NAME);
      assertThat(result).isPresent().contains(sword);
    }

    @Test
    void shouldReturnEmptyForUnknownItemName() {
      Optional<Item> result = marketplace.getItem(UNKNOWN_ITEM_NAME);
      assertThat(result).isEmpty();
    }
  }

  // ── buyItem ──────────────────────────────────────────────────────────────────
  @Nested
  class BuyItem {

    @Test
    void shouldReturnItemPriceOnBuy() {
      Money price = marketplace.buyItem(SWORD_NAME);
      assertThat(price).isEqualTo(SWORD_PRICE);
    }

    @Test
    void shouldReturnCorrectPriceForEachItemType() {
      assertThat(marketplace.buyItem(SHIELD_NAME)).isEqualTo(SHIELD_PRICE);
      assertThat(marketplace.buyItem(HP_POTION_NAME)).isEqualTo(HP_PRICE);
      assertThat(marketplace.buyItem(MP_POTION_NAME)).isEqualTo(MP_PRICE);
    }

    @Test
    void shouldThrowWhenBuyingNonExistentItem() {
      assertThatThrownBy(() -> marketplace.buyItem(UNKNOWN_BUY_ITEM_NAME))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining(UNKNOWN_BUY_ITEM_NAME);
    }
  }

  // ── sellItem ─────────────────────────────────────────────────────────────────
  @Nested
  class SellItem {

    @Test
    void shouldReturnItemPriceOnSell() {
      Money price = marketplace.sellItem(SWORD_NAME);
      assertThat(price).isEqualTo(SWORD_PRICE);
    }

    @Test
    void shouldReturnCorrectPriceForEachItemType() {
      assertThat(marketplace.sellItem(SHIELD_NAME)).isEqualTo(SHIELD_PRICE);
      assertThat(marketplace.sellItem(HP_POTION_NAME)).isEqualTo(HP_PRICE);
      assertThat(marketplace.sellItem(MP_POTION_NAME)).isEqualTo(MP_PRICE);
    }

    @Test
    void shouldThrowWhenSellingNonExistentItem() {
      assertThatThrownBy(() -> marketplace.sellItem(UNKNOWN_SELL_ITEM_NAME))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining(UNKNOWN_SELL_ITEM_NAME);
    }
  }
}
