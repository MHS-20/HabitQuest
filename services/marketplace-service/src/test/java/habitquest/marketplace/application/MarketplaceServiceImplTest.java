package habitquest.marketplace.application;

import static habitquest.marketplace.MarketplaceFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import common.ddd.Id;
import habitquest.marketplace.domain.ItemNotFoundException;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.events.ItemBought;
import habitquest.marketplace.domain.events.ItemSold;
import habitquest.marketplace.domain.events.MarketplaceEvent;
import habitquest.marketplace.domain.events.MarketplaceObserver;
import habitquest.marketplace.domain.factory.MarketplaceFactory;
import habitquest.marketplace.domain.items.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarketplaceServiceImplTest {

  @Mock private MarketplaceRepository marketplaceRepository;
  @Mock private MarketplaceObserver marketplaceObserver;
  @Mock private MarketplaceFactory marketplaceFactory;
  @Mock private Marketplace marketplace;

  @InjectMocks private MarketplaceServiceImpl service;

  private Weapon sword;
  private Armor shield;
  private HealthPotion hpPotion;
  private ManaPotion mpPotion;

  @BeforeEach
  void setUp() {
    sword = sword();
    shield = shield();
    hpPotion = hpPotion();
    mpPotion = mpPotion();
  }

  // ── Stub helpers ─────────────────────────────────────────────────────────────

  private void givenMarketplaceExists() {
    when(marketplaceRepository.findById(MARKETPLACE_MP_ID)).thenReturn(Optional.of(marketplace));
  }

  private void givenMarketplaceExistsWithAvatar() {
    givenMarketplaceExists();
    when(marketplace.getAvatarId()).thenReturn(AVATAR_ID_99);
  }

  private void givenMarketplaceNotFound(Id<Marketplace> id) {
    when(marketplaceRepository.findById(id)).thenReturn(Optional.empty());
  }

  // ── getMarketplace ────────────────────────────────────────────────────────────

  @Nested
  class GetMarketplace {

    @Test
    void shouldReturnMarketplaceWhenFound() {
      givenMarketplaceExists();
      assertThat(service.getMarketplace(MARKETPLACE_MP_ID)).isSameAs(marketplace);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      givenMarketplaceNotFound(UNKNOWN_MARKETPLACE_ID);
      assertThatThrownBy(() -> service.getMarketplace(UNKNOWN_MARKETPLACE_ID))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── getAvatarId ───────────────────────────────────────────────────────────────

  @Nested
  class GetAvatarId {

    @Test
    void shouldReturnAvatarId() {
      givenMarketplaceExistsWithAvatar();
      assertThat(service.getAvatarId(MARKETPLACE_MP_ID)).isEqualTo(AVATAR_ID_99);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      assertThatThrownBy(() -> service.getAvatarId(MISSING_MARKETPLACE_ID))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── getAllAvailableItems ───────────────────────────────────────────────────────

  @Nested
  class GetAllAvailableItems {

    @Test
    void shouldReturnAllAvailableItems() {
      givenMarketplaceExists();
      when(marketplace.getAllAvailableItems())
          .thenReturn(List.of(sword, shield, hpPotion, mpPotion));
      assertThat(service.getAllAvailableItems(MARKETPLACE_MP_ID))
          .containsExactlyInAnyOrder(sword, shield, hpPotion, mpPotion);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      assertThatThrownBy(() -> service.getAllAvailableItems(MISSING_MARKETPLACE_ID))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── getAvailableItemsByType ───────────────────────────────────────────────────

  @Nested
  class GetAvailableItemsByType {

    @Test
    void shouldReturnArmors() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItemsByType(ItemType.ARMOR)).thenReturn(List.of(shield));
      assertThat(service.getAvailableItemsByType(MARKETPLACE_MP_ID, ItemType.ARMOR))
          .containsExactly(shield);
    }

    @Test
    void shouldReturnWeapons() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItemsByType(ItemType.WEAPON)).thenReturn(List.of(sword));
      assertThat(service.getAvailableItemsByType(MARKETPLACE_MP_ID, ItemType.WEAPON))
          .containsExactly(sword);
    }

    @Test
    void shouldReturnPotions() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItemsByType(ItemType.POTION))
          .thenReturn(List.of(hpPotion, mpPotion));
      assertThat(service.getAvailableItemsByType(MARKETPLACE_MP_ID, ItemType.POTION))
          .containsExactlyInAnyOrder(hpPotion, mpPotion);
    }

    @Test
    void shouldReturnHealthPotions() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItemsByType(ItemType.HEALTH_POTION))
          .thenReturn(List.of(hpPotion));
      assertThat(service.getAvailableItemsByType(MARKETPLACE_MP_ID, ItemType.HEALTH_POTION))
          .containsExactly(hpPotion);
    }

    @Test
    void shouldReturnManaPotions() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItemsByType(ItemType.MANA_POTION)).thenReturn(List.of(mpPotion));
      assertThat(service.getAvailableItemsByType(MARKETPLACE_MP_ID, ItemType.MANA_POTION))
          .containsExactly(mpPotion);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      assertThatThrownBy(
              () -> service.getAvailableItemsByType(MISSING_MARKETPLACE_ID, ItemType.ALL))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── getAvailableItem ──────────────────────────────────────────────────────────

  @Nested
  class GetAvailableItem {

    @Test
    void shouldReturnItemWhenFound() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      assertThat(service.getAvailableItem(MARKETPLACE_MP_ID, SWORD_NAME)).isEqualTo(sword);
    }

    @Test
    void shouldThrowItemNotFoundWhenNameUnknown() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItem(UNKNOWN_ITEM_NAME)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> service.getAvailableItem(MARKETPLACE_MP_ID, UNKNOWN_ITEM_NAME))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowItemNotFoundWhenItemAlreadyBought() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> service.getAvailableItem(MARKETPLACE_MP_ID, SWORD_NAME))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowMarketplaceNotFoundWhenMarketplaceMissing() {
      givenMarketplaceNotFound(GHOST_MARKETPLACE_ID);
      assertThatThrownBy(() -> service.getAvailableItem(GHOST_MARKETPLACE_ID, SWORD_NAME))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── getSoldItems ──────────────────────────────────────────────────────────────

  @Nested
  class GetSoldItems {

    @Test
    void shouldReturnEmptyWhenNothingBought() {
      givenMarketplaceExists();
      when(marketplace.getSoldItems()).thenReturn(List.of());
      assertThat(service.getSoldItems(MARKETPLACE_MP_ID)).isEmpty();
    }

    @Test
    void shouldReturnAllSoldItems() {
      givenMarketplaceExists();
      when(marketplace.getSoldItems()).thenReturn(List.of(sword, shield));
      assertThat(service.getSoldItems(MARKETPLACE_MP_ID)).containsExactlyInAnyOrder(sword, shield);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      assertThatThrownBy(() -> service.getSoldItems(MISSING_MARKETPLACE_ID))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── getSoldItem ───────────────────────────────────────────────────────────────

  @Nested
  class GetSoldItem {

    @Test
    void shouldReturnItemAfterBuy() {
      givenMarketplaceExists();
      when(marketplace.getSoldItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      assertThat(service.getSoldItem(MARKETPLACE_MP_ID, SWORD_NAME)).isEqualTo(sword);
    }

    @Test
    void shouldThrowItemNotFoundWhenItemNotBoughtYet() {
      givenMarketplaceExists();
      when(marketplace.getSoldItem(SWORD_NAME)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> service.getSoldItem(MARKETPLACE_MP_ID, SWORD_NAME))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowItemNotFoundForUnknownName() {
      givenMarketplaceExists();
      when(marketplace.getSoldItem(UNKNOWN_ITEM_NAME)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> service.getSoldItem(MARKETPLACE_MP_ID, UNKNOWN_ITEM_NAME))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowMarketplaceNotFoundWhenMarketplaceMissing() {
      givenMarketplaceNotFound(GHOST_MARKETPLACE_ID);
      assertThatThrownBy(() -> service.getSoldItem(GHOST_MARKETPLACE_ID, SWORD_NAME))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── buyItem ───────────────────────────────────────────────────────────────────

  @Nested
  class BuyItem {

    @Test
    void shouldSaveMarketplaceAfterBuy() {
      givenMarketplaceExistsWithAvatar();
      service.buyItem(MARKETPLACE_MP_ID, SWORD_NAME);
      verify(marketplaceRepository).save(marketplace);
    }

    @Test
    void shouldPublishItemBoughtEvent() {
      givenMarketplaceExistsWithAvatar();
      service.buyItem(MARKETPLACE_MP_ID, SWORD_NAME);
      ArgumentCaptor<ItemBought> captor = ArgumentCaptor.forClass(ItemBought.class);
      verify(marketplaceObserver).notifyMarketplaceEvent(captor.capture());
      ItemBought event = captor.getValue();
      assertThat(event.marketplaceId()).isEqualTo(MARKETPLACE_MP_ID);
      assertThat(event.itemName()).isEqualTo(SWORD_NAME);
      assertThat(event.avatarId()).isEqualTo(AVATAR_ID_99);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      assertThatThrownBy(() -> service.buyItem(MISSING_MARKETPLACE_ID, SWORD_NAME))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }

    @Test
    void shouldNeverPublishEventWhenMarketplaceMissing() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      ignoreThrows(() -> service.buyItem(MISSING_MARKETPLACE_ID, SWORD_NAME));
      verifyNoInteractions(marketplaceObserver);
    }
  }

  // ── sellItem ──────────────────────────────────────────────────────────────────

  @Nested
  class SellItem {

    @Test
    void shouldSaveMarketplaceAfterSell() {
      givenMarketplaceExistsWithAvatar();
      service.sellItem(MARKETPLACE_MP_ID, SWORD_NAME);
      verify(marketplaceRepository).save(marketplace);
    }

    @Test
    void shouldPublishItemSoldEvent() {
      givenMarketplaceExistsWithAvatar();
      service.sellItem(MARKETPLACE_MP_ID, SWORD_NAME);
      ArgumentCaptor<ItemSold> captor = ArgumentCaptor.forClass(ItemSold.class);
      verify(marketplaceObserver).notifyMarketplaceEvent(captor.capture());
      ItemSold event = captor.getValue();
      assertThat(event.marketplaceId()).isEqualTo(MARKETPLACE_MP_ID);
      assertThat(event.itemName()).isEqualTo(SWORD_NAME);
      assertThat(event.avatarId()).isEqualTo(AVATAR_ID_99);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      assertThatThrownBy(() -> service.sellItem(MISSING_MARKETPLACE_ID, SWORD_NAME))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }

    @Test
    void shouldNeverPublishEventWhenMarketplaceMissing() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      ignoreThrows(() -> service.sellItem(MISSING_MARKETPLACE_ID, SWORD_NAME));
      verifyNoInteractions(marketplaceObserver);
    }
  }

  // ── canBuyItem ────────────────────────────────────────────────────────────────

  @Nested
  class CanBuyItem {

    @Test
    void shouldReturnTrueWhenPlayerLevelMeetsRequirement() {
      givenMarketplaceExists();
      // sword requires level 1, player is level 1 — exact match
      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      assertThat(service.canBuyItem(MARKETPLACE_MP_ID, SWORD_NAME, LEVEL_1)).isTrue();
    }

    @Test
    void shouldReturnTrueWhenPlayerLevelExceedsRequirement() {
      givenMarketplaceExists();
      // sword requires level 1, player is level 10 — well above
      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      assertThat(service.canBuyItem(MARKETPLACE_MP_ID, SWORD_NAME, LEVEL_10)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPlayerLevelBelowRequirement() {
      givenMarketplaceExists();
      // high-level item requires level 10, player is only level 1
      when(marketplace.getAvailableItem("Elite Sword")).thenReturn(Optional.of(eliteSword()));
      assertThat(service.canBuyItem(MARKETPLACE_MP_ID, "Elite Sword", LEVEL_1)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenPlayerLevelIsOneBelow() {
      givenMarketplaceExists();
      // item requires level 5, player is level 4 — boundary case
      when(marketplace.getAvailableItem("Mid Sword")).thenReturn(Optional.of(midSword()));
      assertThat(service.canBuyItem(MARKETPLACE_MP_ID, "Mid Sword", new Level(4))).isFalse();
    }

    @Test
    void shouldThrowItemNotFoundWhenItemDoesNotExist() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItem(UNKNOWN_ITEM_NAME)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> service.canBuyItem(MARKETPLACE_MP_ID, UNKNOWN_ITEM_NAME, LEVEL_1))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowMarketplaceNotFoundWhenMarketplaceMissing() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      assertThatThrownBy(() -> service.canBuyItem(MISSING_MARKETPLACE_ID, SWORD_NAME, LEVEL_1))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── Observer delegation ───────────────────────────────────────────────────────

  @Nested
  class ObserverDelegation {

    @Test
    void buyAndSellShouldEachPublishExactlyOneEvent() {
      givenMarketplaceExistsWithAvatar();
      service.buyItem(MARKETPLACE_MP_ID, SWORD_NAME);
      service.sellItem(MARKETPLACE_MP_ID, SWORD_NAME);
      verify(marketplaceObserver, times(2)).notifyMarketplaceEvent(any());
    }

    @Test
    void buyEventAndSellEventShouldHaveDifferentTypes() {
      givenMarketplaceExistsWithAvatar();
      service.buyItem(MARKETPLACE_MP_ID, SWORD_NAME);
      service.sellItem(MARKETPLACE_MP_ID, SWORD_NAME);
      ArgumentCaptor<MarketplaceEvent> captor = ArgumentCaptor.forClass(MarketplaceEvent.class);
      verify(marketplaceObserver, times(2)).notifyMarketplaceEvent(captor.capture());
      List<MarketplaceEvent> events = captor.getAllValues();
      assertThat(events.get(0)).isInstanceOf(ItemBought.class);
      assertThat(events.get(1)).isInstanceOf(ItemSold.class);
    }
  }

  // ── Helpers ───────────────────────────────────────────────────────────────────
  private void ignoreThrows(Runnable action) {
    try {
      action.run();
    } catch (MarketplaceNotFoundException ignored) {
    }
  }
}
