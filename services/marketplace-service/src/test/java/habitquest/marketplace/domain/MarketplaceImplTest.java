package habitquest.marketplace.domain;

import static habitquest.marketplace.MarketplaceFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import common.ddd.Id;
import habitquest.marketplace.domain.exceptions.ItemNotFoundException;
import habitquest.marketplace.domain.items.*;
import habitquest.marketplace.domain.marketplace.MarketplaceImpl;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MarketplaceImplTest {

  private MarketplaceImpl marketplace;

  @BeforeEach
  void setUp() {
    ItemCatalog catalog = mockCatalog();
    marketplace = new MarketplaceImpl(MARKETPLACE_ID, AVATAR_ID, catalog);
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
    MarketplaceImpl empty =
        new MarketplaceImpl(new Id<>("empty"), new Id<>(AVATAR_1), emptyCatalog);
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
      marketplace.buyItem(SWORD_NAME);
      assertThat(marketplace.getAllAvailableItems())
          .containsExactlyInAnyOrder(shield(), hpPotion(), mpPotion())
          .doesNotContain(sword());
    }

    @Test
    void shouldExcludeBoughtItemsFromAvailableItemsByType() {
      marketplace.buyItem(SWORD_NAME);
      assertThat(marketplace.getAvailableItemsByType(ItemFilter.WEAPON)).isEmpty();
    }
  }

  // ── getAvailableItem ─────────────────────────────────────────────────────────

  @Nested
  class GetAvailableItem {

    @Test
    void shouldFindExistingAvailableItemByName() {
      assertThat(marketplace.getAvailableItem(SWORD_NAME)).isPresent().contains(sword());
    }

    @Test
    void shouldReturnEmptyForUnknownItemName() {
      assertThat(marketplace.getAvailableItem(UNKNOWN_ITEM_NAME)).isEmpty();
    }

    @Test
    void shouldReturnEmptyForAlreadyBoughtItem() {
      marketplace.buyItem(SWORD_NAME);
      assertThat(marketplace.getAvailableItem(SWORD_NAME)).isEmpty();
    }
  }

  // ── getSoldItem / getSoldItems ────────────────────────────────────────────────

  @Nested
  class GetSoldItems {

    @Test
    void shouldReturnEmptyForItemNotYetBought() {
      assertThat(marketplace.getSoldItem(SWORD_NAME)).isEmpty();
    }

    @Test
    void shouldFindSoldItemAfterBuy() {
      marketplace.buyItem(SWORD_NAME);
      assertThat(marketplace.getSoldItem(SWORD_NAME)).isPresent().contains(sword());
    }

    @Test
    void shouldReturnAllSoldItems() {
      marketplace.buyItem(SWORD_NAME);
      marketplace.buyItem(SHIELD_NAME);
      assertThat(marketplace.getSoldItems()).containsExactlyInAnyOrder(sword(), shield());
    }

    @Test
    void shouldRemoveItemFromSoldItemsAfterSell() {
      marketplace.buyItem(SWORD_NAME);
      marketplace.sellItem(SWORD_NAME);
      assertThat(marketplace.getSoldItem(SWORD_NAME)).isEmpty();
      assertThat(marketplace.getSoldItems()).doesNotContain(sword());
    }
  }

  // ── buyItem ──────────────────────────────────────────────────────────────────

  @Nested
  class BuyItem {

    @Test
    void shouldReturnItemPriceOnBuy() {
      assertThat(marketplace.buyItem(SWORD_NAME)).isEqualTo(SWORD_PRICE);
    }

    @Test
    void shouldReturnCorrectPriceForEachItemType() {
      assertThat(marketplace.buyItem(SHIELD_NAME)).isEqualTo(SHIELD_PRICE);
      assertThat(marketplace.buyItem(HP_POTION_NAME)).isEqualTo(HP_PRICE);
      assertThat(marketplace.buyItem(MP_POTION_NAME)).isEqualTo(MP_PRICE);
    }

    @Test
    void shouldMoveItemFromAvailableToSoldAfterBuy() {
      marketplace.buyItem(SWORD_NAME);
      assertThat(marketplace.getAvailableItem(SWORD_NAME)).isEmpty();
      assertThat(marketplace.getSoldItem(SWORD_NAME)).isPresent().contains(sword());
    }

    @Test
    void shouldThrowWhenBuyingNonExistentItem() {
      assertThatThrownBy(() -> marketplace.buyItem(UNKNOWN_ITEM_NAME))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowWhenBuyingAlreadyBoughtItem() {
      marketplace.buyItem(SWORD_NAME);
      assertThatThrownBy(() -> marketplace.buyItem(SWORD_NAME))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining(SWORD_NAME);
    }
  }

  // ── sellItem ─────────────────────────────────────────────────────────────────

  @Nested
  class SellItem {

    @Test
    void shouldReturnItemPriceOnSell() {
      marketplace.buyItem(SWORD_NAME);
      assertThat(marketplace.sellItem(SWORD_NAME)).isEqualTo(SWORD_PRICE);
    }

    @Test
    void shouldReturnCorrectPriceForEachItemType() {
      marketplace.buyItem(SHIELD_NAME);
      marketplace.buyItem(HP_POTION_NAME);
      marketplace.buyItem(MP_POTION_NAME);
      assertThat(marketplace.sellItem(SHIELD_NAME)).isEqualTo(SHIELD_PRICE);
      assertThat(marketplace.sellItem(HP_POTION_NAME)).isEqualTo(HP_PRICE);
      assertThat(marketplace.sellItem(MP_POTION_NAME)).isEqualTo(MP_PRICE);
    }

    @Test
    void shouldMoveItemFromSoldToAvailableAfterSell() {
      marketplace.buyItem(SWORD_NAME);
      marketplace.sellItem(SWORD_NAME);
      assertThat(marketplace.getSoldItem(SWORD_NAME)).isEmpty();
      assertThat(marketplace.getAvailableItem(SWORD_NAME)).isPresent().contains(sword());
    }

    @Test
    void shouldThrowWhenSellingNonExistentItem() {
      assertThatThrownBy(() -> marketplace.sellItem(UNKNOWN_ITEM_NAME))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowWhenSellingItemNotYetBought() {
      assertThatThrownBy(() -> marketplace.sellItem(SWORD_NAME))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining(SWORD_NAME);
    }
  }
}
