package habitquest.marketplace.domain;

import static habitquest.marketplace.MarketplaceFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import common.ddd.Id;
import habitquest.marketplace.domain.exceptions.ItemNotFoundException;
import habitquest.marketplace.domain.items.*;
import habitquest.marketplace.domain.marketplace.Marketplace;
import habitquest.marketplace.domain.marketplace.Money;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MarketplaceTest {

  private Marketplace marketplace;

  @BeforeEach
  void setUp() {
    ItemCatalog catalog = mockCatalog();
    marketplace = new Marketplace(MARKETPLACE_ID, AVATAR_ID, catalog);
  }

  // ── Identity ─────────────────────────────────────────────────────────────────

  @Test
  void shouldReturnCorrectId() {
    assertThat(marketplace.getId().value()).isEqualTo(MARKETPLACE_1);
  }

  @Test
  void shouldReturnCorrectAvatarId() {
    assertThat(marketplace.getAvatarId().value()).isEqualTo(AVATAR_1);
  }

  // ── Empty marketplace ─────────────────────────────────────────────────────────

  @Test
  void emptyMarketplaceShouldHaveNoAvailableItems() {
    ItemCatalog emptyCatalog = mock(ItemCatalog.class);
    when(emptyCatalog.getAllItems()).thenReturn(List.of());
    Marketplace empty = new Marketplace(new Id<>("empty"), new Id<>(AVATAR_1), emptyCatalog);
    assertThat(empty.getAllAvailableItems()).isEmpty();
  }

  @Test
  void emptyMarketplaceShouldHaveNoSoldItems() {
    assertThat(marketplace.getSoldItems()).isEmpty();
  }

  // ── getAllAvailableItems / getAvailableItemsByType ────────────────────────────

  @Nested
  class GetAvailableItems {

    @Test
    void shouldReturnAllAvailableItems() {
      assertThat(marketplace.getAllAvailableItems())
          .containsExactlyInAnyOrder(sword(), shield(), hpPotion(), mpPotion());
    }

    @Test
    void shouldReturnAvailableArmors() {
      assertThat(marketplace.getAvailableItemsByType(ItemFilter.ARMOR)).containsExactly(shield());
    }

    @Test
    void shouldReturnAvailableWeapons() {
      assertThat(marketplace.getAvailableItemsByType(ItemFilter.WEAPON)).containsExactly(sword());
    }

    @Test
    void shouldReturnAllAvailablePotions() {
      assertThat(marketplace.getAvailableItemsByType(ItemFilter.POTION))
          .containsExactlyInAnyOrder(hpPotion(), mpPotion());
    }

    @Test
    void shouldReturnAvailableHealthPotions() {
      assertThat(marketplace.getAvailableItemsByType(ItemFilter.HEALTH_POTION))
          .containsExactly(hpPotion());
    }

    @Test
    void shouldReturnAvailableManaPotions() {
      assertThat(marketplace.getAvailableItemsByType(ItemFilter.MANA_POTION))
          .containsExactly(mpPotion());
    }

    @Test
    void shouldExcludeBoughtItemsFromAvailableItems() {
      marketplace.buyItem(sword());
      assertThat(marketplace.getAllAvailableItems())
          .containsExactlyInAnyOrder(shield(), hpPotion(), mpPotion())
          .doesNotContain(sword());
    }

    @Test
    void shouldExcludeBoughtItemsFromAvailableItemsByType() {
      marketplace.buyItem(sword());
      assertThat(marketplace.getAvailableItemsByType(ItemFilter.WEAPON)).isEmpty();
    }
  }

  // ── getAvailableItem ─────────────────────────────────────────────────────────

  @Nested
  class GetAvailableItem {

    @Test
    void shouldFindExistingAvailableItem() {
      assertThat(marketplace.getAvailableItem(sword())).isPresent().contains(sword());
    }

    @Test
    void shouldReturnEmptyForUnknownItem() {
      Weapon unknown = new Weapon(UNKNOWN_ITEM_NAME, "???", 0, new Money(0), LEVEL_1);
      assertThat(marketplace.getAvailableItem(unknown)).isEmpty();
    }

    @Test
    void shouldReturnEmptyForAlreadyBoughtItem() {
      marketplace.buyItem(sword());
      assertThat(marketplace.getAvailableItem(sword())).isEmpty();
    }
  }

  // ── getSoldItem / getSoldItems ────────────────────────────────────────────────

  @Nested
  class GetSoldItems {

    @Test
    void shouldReturnEmptyForItemNotYetBought() {
      assertThat(marketplace.getSoldItem(sword())).isEmpty();
    }

    @Test
    void shouldFindSoldItemAfterBuy() {
      marketplace.buyItem(sword());
      assertThat(marketplace.getSoldItem(sword())).isPresent().contains(sword());
    }

    @Test
    void shouldReturnAllSoldItems() {
      marketplace.buyItem(sword());
      marketplace.buyItem(shield());
      assertThat(marketplace.getSoldItems()).containsExactlyInAnyOrder(sword(), shield());
    }

    @Test
    void shouldRemoveItemFromSoldItemsAfterSell() {
      marketplace.buyItem(sword());
      marketplace.sellItem(sword());
      assertThat(marketplace.getSoldItem(sword())).isEmpty();
      assertThat(marketplace.getSoldItems()).doesNotContain(sword());
    }
  }

  // ── buyItem ──────────────────────────────────────────────────────────────────

  @Nested
  class BuyItem {

    @Test
    void shouldReturnItemPriceOnBuy() {
      assertThat(marketplace.buyItem(sword())).isEqualTo(SWORD_PRICE);
    }

    @Test
    void shouldReturnCorrectPriceForEachItemType() {
      assertThat(marketplace.buyItem(shield())).isEqualTo(SHIELD_PRICE);
      assertThat(marketplace.buyItem(hpPotion())).isEqualTo(HP_PRICE);
      assertThat(marketplace.buyItem(mpPotion())).isEqualTo(MP_PRICE);
    }

    @Test
    void shouldMoveItemFromAvailableToSoldAfterBuy() {
      marketplace.buyItem(sword());
      assertThat(marketplace.getAvailableItem(sword())).isEmpty();
      assertThat(marketplace.getSoldItem(sword())).isPresent().contains(sword());
    }

    @Test
    void shouldThrowWhenBuyingNonExistentItem() {
      Weapon unknown = new Weapon(UNKNOWN_ITEM_NAME, "???", 0, new Money(0), LEVEL_1);
      assertThatThrownBy(() -> marketplace.buyItem(unknown))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowWhenBuyingAlreadyBoughtItem() {
      marketplace.buyItem(sword());
      assertThatThrownBy(() -> marketplace.buyItem(sword()))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining(SWORD_NAME);
    }
  }

  // ── sellItem ─────────────────────────────────────────────────────────────────

  @Nested
  class SellItem {

    @Test
    void shouldReturnItemPriceOnSell() {
      marketplace.buyItem(sword());
      assertThat(marketplace.sellItem(sword())).isEqualTo(SWORD_PRICE);
    }

    @Test
    void shouldReturnCorrectPriceForEachItemType() {
      marketplace.buyItem(shield());
      marketplace.buyItem(hpPotion());
      marketplace.buyItem(mpPotion());
      assertThat(marketplace.sellItem(shield())).isEqualTo(SHIELD_PRICE);
      assertThat(marketplace.sellItem(hpPotion())).isEqualTo(HP_PRICE);
      assertThat(marketplace.sellItem(mpPotion())).isEqualTo(MP_PRICE);
    }

    @Test
    void shouldMoveItemFromSoldToAvailableAfterSell() {
      marketplace.buyItem(sword());
      marketplace.sellItem(sword());
      assertThat(marketplace.getSoldItem(sword())).isEmpty();
      assertThat(marketplace.getAvailableItem(sword())).isPresent().contains(sword());
    }

    @Test
    void shouldThrowWhenSellingNonExistentItem() {
      Weapon unknown = new Weapon(UNKNOWN_ITEM_NAME, "???", 0, new Money(0), LEVEL_1);
      assertThatThrownBy(() -> marketplace.sellItem(unknown))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowWhenSellingItemNotYetBought() {
      assertThatThrownBy(() -> marketplace.sellItem(sword()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining(SWORD_NAME);
    }
  }
}
