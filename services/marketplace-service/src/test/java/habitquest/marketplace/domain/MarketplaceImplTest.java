package habitquest.marketplace.domain;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import common.ddd.Id;
import habitquest.marketplace.domain.items.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class MarketplaceImplTest {

  private static final String MARKETPLACE_ID = "mp-1";
  private static final String AVATAR_ID = "avatar-1";
  private static final String SWORD_NAME = "Iron Sword";
  private static final String SHIELD_NAME = "Iron Shield";
  private static final String HP_POTION_NAME = "HP Potion";
  private static final String MP_POTION_NAME = "MP Potion";
  private static final String SWORD_DESC = "A basic sword";
  private static final String SHIELD_DESC = "A basic shield";
  private static final String HP_POTION_DESC = "Restores 50 HP";
  private static final String MP_POTION_DESC = "Restores 30 MP";
  private static final String UNKNOWN_ITEM_NAME = "Legendary Axe";
  private static final Money SWORD_PRICE = new Money(50);
  private static final Money SHIELD_PRICE = new Money(30);
  private static final Money HP_PRICE = new Money(10);
  private static final Money MP_PRICE = new Money(12);
  private static final Level LEVEL_1 = new Level(1);

  private Weapon sword;
  private Armor shield;
  private HealthPotion hpPotion;
  private ManaPotion mpPotion;
  private ItemCatalog catalog;
  private MarketplaceImpl marketplace;

  @BeforeEach
  void setUp() {
    sword = new Weapon(SWORD_NAME, SWORD_DESC, 10, SWORD_PRICE, LEVEL_1);
    shield = new Armor(SHIELD_NAME, SHIELD_DESC, 5, SHIELD_PRICE, LEVEL_1);
    hpPotion = new HealthPotion(HP_POTION_NAME, HP_POTION_DESC, 50, HP_PRICE, LEVEL_1);
    mpPotion = new ManaPotion(MP_POTION_NAME, MP_POTION_DESC, 30, MP_PRICE, LEVEL_1);

    catalog = mock(ItemCatalog.class);
    when(catalog.getAllItems()).thenReturn(List.of(sword, shield, hpPotion, mpPotion));
    when(catalog.getItemsByType(ItemType.ALL))
        .thenReturn(List.of(sword, shield, hpPotion, mpPotion));
    when(catalog.getItemsByType(ItemType.ARMOR)).thenReturn(List.of(shield));
    when(catalog.getItemsByType(ItemType.WEAPON)).thenReturn(List.of(sword));
    when(catalog.getItemsByType(ItemType.POTION)).thenReturn(List.of(hpPotion, mpPotion));
    when(catalog.getItemsByType(ItemType.HEALTH_POTION)).thenReturn(List.of(hpPotion));
    when(catalog.getItemsByType(ItemType.MANA_POTION)).thenReturn(List.of(mpPotion));
    when(catalog.getItem(SWORD_NAME)).thenReturn(Optional.of(sword));
    when(catalog.getItem(SHIELD_NAME)).thenReturn(Optional.of(shield));
    when(catalog.getItem(HP_POTION_NAME)).thenReturn(Optional.of(hpPotion));
    when(catalog.getItem(MP_POTION_NAME)).thenReturn(Optional.of(mpPotion));
    when(catalog.getItem(UNKNOWN_ITEM_NAME)).thenReturn(Optional.empty());
    when(catalog.contains(SWORD_NAME)).thenReturn(true);
    when(catalog.contains(SHIELD_NAME)).thenReturn(true);
    when(catalog.contains(HP_POTION_NAME)).thenReturn(true);
    when(catalog.contains(MP_POTION_NAME)).thenReturn(true);
    when(catalog.contains(UNKNOWN_ITEM_NAME)).thenReturn(false);

    marketplace = new MarketplaceImpl(new Id<>(MARKETPLACE_ID), new Id<>(AVATAR_ID), catalog);
  }

  // ── Identity ─────────────────────────────────────────────────────────────────

  @Test
  void shouldReturnCorrectId() {
    assertThat(marketplace.getId().value()).isEqualTo(MARKETPLACE_ID);
  }

  @Test
  void shouldReturnCorrectAvatarId() {
    assertThat(marketplace.getAvatarId().value()).isEqualTo(AVATAR_ID);
  }

  // ── Empty marketplace ─────────────────────────────────────────────────────────

  @Test
  void emptyMarketplaceShouldHaveNoAvailableItems() {
    ItemCatalog emptyCatalog = mock(ItemCatalog.class);
    when(emptyCatalog.getAllItems()).thenReturn(List.of());
    MarketplaceImpl empty =
        new MarketplaceImpl(new Id<>("empty"), new Id<>(AVATAR_ID), emptyCatalog);
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
          .containsExactlyInAnyOrder(sword, shield, hpPotion, mpPotion);
    }

    @Test
    void shouldReturnAvailableArmors() {
      assertThat(marketplace.getAvailableItemsByType(ItemType.ARMOR)).containsExactly(shield);
    }

    @Test
    void shouldReturnAvailableWeapons() {
      assertThat(marketplace.getAvailableItemsByType(ItemType.WEAPON)).containsExactly(sword);
    }

    @Test
    void shouldReturnAllAvailablePotions() {
      assertThat(marketplace.getAvailableItemsByType(ItemType.POTION))
          .containsExactlyInAnyOrder(hpPotion, mpPotion);
    }

    @Test
    void shouldReturnAvailableHealthPotions() {
      assertThat(marketplace.getAvailableItemsByType(ItemType.HEALTH_POTION))
          .containsExactly(hpPotion);
    }

    @Test
    void shouldReturnAvailableManaPotions() {
      assertThat(marketplace.getAvailableItemsByType(ItemType.MANA_POTION))
          .containsExactly(mpPotion);
    }

    @Test
    void shouldExcludeBoughtItemsFromAvailableItems() {
      marketplace.buyItem(SWORD_NAME);
      assertThat(marketplace.getAllAvailableItems())
          .containsExactlyInAnyOrder(shield, hpPotion, mpPotion)
          .doesNotContain(sword);
    }

    @Test
    void shouldExcludeBoughtItemsFromAvailableItemsByType() {
      marketplace.buyItem(SWORD_NAME);
      assertThat(marketplace.getAvailableItemsByType(ItemType.WEAPON)).isEmpty();
    }
  }

  // ── getAvailableItem ─────────────────────────────────────────────────────────

  @Nested
  class GetAvailableItem {

    @Test
    void shouldFindExistingAvailableItemByName() {
      assertThat(marketplace.getAvailableItem(SWORD_NAME)).isPresent().contains(sword);
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
      assertThat(marketplace.getSoldItem(SWORD_NAME)).isPresent().contains(sword);
    }

    @Test
    void shouldReturnAllSoldItems() {
      marketplace.buyItem(SWORD_NAME);
      marketplace.buyItem(SHIELD_NAME);
      assertThat(marketplace.getSoldItems()).containsExactlyInAnyOrder(sword, shield);
    }

    @Test
    void shouldRemoveItemFromSoldItemsAfterSell() {
      marketplace.buyItem(SWORD_NAME);
      marketplace.sellItem(SWORD_NAME);
      assertThat(marketplace.getSoldItem(SWORD_NAME)).isEmpty();
      assertThat(marketplace.getSoldItems()).doesNotContain(sword);
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
      assertThat(marketplace.getSoldItem(SWORD_NAME)).isPresent().contains(sword);
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
      assertThat(marketplace.getAvailableItem(SWORD_NAME)).isPresent().contains(sword);
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
