package habitquest.marketplace.application;

import static habitquest.marketplace.MarketplaceFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.inOrder;

import common.ddd.Id;
import habitquest.marketplace.application.exceptions.AvatarCommunicationException;
import habitquest.marketplace.application.exceptions.InsufficientLevelException;
import habitquest.marketplace.application.exceptions.MarketplaceNotFoundException;
import habitquest.marketplace.application.port.out.AvatarClientPort;
import habitquest.marketplace.application.port.out.MarketplaceRepository;
import habitquest.marketplace.application.service.MarketplaceCommandServiceImpl;
import habitquest.marketplace.application.service.MarketplaceQueryServiceImpl;
import habitquest.marketplace.domain.events.ItemBought;
import habitquest.marketplace.domain.events.ItemSold;
import habitquest.marketplace.domain.events.MarketplaceEvent;
import habitquest.marketplace.domain.events.MarketplaceObserver;
import habitquest.marketplace.domain.exceptions.ItemNotFoundException;
import habitquest.marketplace.domain.factory.MarketplaceFactory;
import habitquest.marketplace.domain.items.*;
import habitquest.marketplace.domain.marketplace.Marketplace;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarketplaceServiceImplTest {

  @Mock private MarketplaceRepository marketplaceRepository;
  @Mock private MarketplaceObserver marketplaceObserver;
  @Mock private MarketplaceFactory marketplaceFactory;
  @Mock private Marketplace marketplace;
  @Mock private AvatarClientPort avatarPort;

  private MarketplaceCommandServiceImpl commandService;
  private MarketplaceQueryServiceImpl queryService;

  private Weapon sword;
  private Weapon eliteSword;
  private Armor shield;
  private HealthPotion hpPotion;
  private ManaPotion mpPotion;

  @BeforeEach
  void setUp() {
    queryService = new MarketplaceQueryServiceImpl(marketplaceRepository);
    commandService =
        new MarketplaceCommandServiceImpl(
            marketplaceRepository,
            marketplaceObserver,
            marketplaceFactory,
            avatarPort,
            queryService);

    sword = sword();
    shield = shield();
    hpPotion = hpPotion();
    mpPotion = mpPotion();
    eliteSword = eliteSword();
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
      assertThat(queryService.getMarketplace(MARKETPLACE_MP_ID)).isSameAs(marketplace);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      givenMarketplaceNotFound(UNKNOWN_MARKETPLACE_ID);
      assertThatThrownBy(() -> queryService.getMarketplace(UNKNOWN_MARKETPLACE_ID))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── getAvatarId ───────────────────────────────────────────────────────────────

  @Nested
  class GetAvatarId {

    @Test
    void shouldReturnAvatarId() {
      givenMarketplaceExistsWithAvatar();
      assertThat(queryService.getAvatarId(MARKETPLACE_MP_ID)).isEqualTo(AVATAR_ID_99);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      assertThatThrownBy(() -> queryService.getAvatarId(MISSING_MARKETPLACE_ID))
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
      assertThat(queryService.getAllAvailableItems(MARKETPLACE_MP_ID))
          .containsExactlyInAnyOrder(sword, shield, hpPotion, mpPotion);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      assertThatThrownBy(() -> queryService.getAllAvailableItems(MISSING_MARKETPLACE_ID))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── getAvailableItemsByType ───────────────────────────────────────────────────

  @Nested
  class GetAvailableItemsByType {

    @Test
    void shouldReturnArmors() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItemsByType(ItemFilter.ARMOR)).thenReturn(List.of(shield));
      assertThat(queryService.getAvailableItemsByType(MARKETPLACE_MP_ID, ItemFilter.ARMOR))
          .containsExactly(shield);
    }

    @Test
    void shouldReturnWeapons() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItemsByType(ItemFilter.WEAPON)).thenReturn(List.of(sword));
      assertThat(queryService.getAvailableItemsByType(MARKETPLACE_MP_ID, ItemFilter.WEAPON))
          .containsExactly(sword);
    }

    @Test
    void shouldReturnPotions() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItemsByType(ItemFilter.POTION))
          .thenReturn(List.of(hpPotion, mpPotion));
      assertThat(queryService.getAvailableItemsByType(MARKETPLACE_MP_ID, ItemFilter.POTION))
          .containsExactlyInAnyOrder(hpPotion, mpPotion);
    }

    @Test
    void shouldReturnHealthPotions() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItemsByType(ItemFilter.HEALTH_POTION))
          .thenReturn(List.of(hpPotion));
      assertThat(queryService.getAvailableItemsByType(MARKETPLACE_MP_ID, ItemFilter.HEALTH_POTION))
          .containsExactly(hpPotion);
    }

    @Test
    void shouldReturnManaPotions() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItemsByType(ItemFilter.MANA_POTION))
          .thenReturn(List.of(mpPotion));
      assertThat(queryService.getAvailableItemsByType(MARKETPLACE_MP_ID, ItemFilter.MANA_POTION))
          .containsExactly(mpPotion);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      assertThatThrownBy(
              () -> queryService.getAvailableItemsByType(MISSING_MARKETPLACE_ID, ItemFilter.ALL))
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
      assertThat(queryService.getAvailableItem(MARKETPLACE_MP_ID, SWORD_NAME)).isEqualTo(sword);
    }

    @Test
    void shouldThrowItemNotFoundWhenNameUnknown() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItem(UNKNOWN_ITEM_NAME)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> queryService.getAvailableItem(MARKETPLACE_MP_ID, UNKNOWN_ITEM_NAME))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowItemNotFoundWhenItemAlreadyBought() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> queryService.getAvailableItem(MARKETPLACE_MP_ID, SWORD_NAME))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowMarketplaceNotFoundWhenMarketplaceMissing() {
      givenMarketplaceNotFound(GHOST_MARKETPLACE_ID);
      assertThatThrownBy(() -> queryService.getAvailableItem(GHOST_MARKETPLACE_ID, SWORD_NAME))
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
      assertThat(queryService.getSoldItems(MARKETPLACE_MP_ID)).isEmpty();
    }

    @Test
    void shouldReturnAllSoldItems() {
      givenMarketplaceExists();
      when(marketplace.getSoldItems()).thenReturn(List.of(sword, shield));
      assertThat(queryService.getSoldItems(MARKETPLACE_MP_ID))
          .containsExactlyInAnyOrder(sword, shield);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      assertThatThrownBy(() -> queryService.getSoldItems(MISSING_MARKETPLACE_ID))
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
      assertThat(queryService.getSoldItem(MARKETPLACE_MP_ID, SWORD_NAME)).isEqualTo(sword);
    }

    @Test
    void shouldThrowItemNotFoundWhenItemNotBoughtYet() {
      givenMarketplaceExists();
      when(marketplace.getSoldItem(SWORD_NAME)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> queryService.getSoldItem(MARKETPLACE_MP_ID, SWORD_NAME))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowItemNotFoundForUnknownName() {
      givenMarketplaceExists();
      when(marketplace.getSoldItem(UNKNOWN_ITEM_NAME)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> queryService.getSoldItem(MARKETPLACE_MP_ID, UNKNOWN_ITEM_NAME))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowMarketplaceNotFoundWhenMarketplaceMissing() {
      givenMarketplaceNotFound(GHOST_MARKETPLACE_ID);
      assertThatThrownBy(() -> queryService.getSoldItem(GHOST_MARKETPLACE_ID, SWORD_NAME))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── BuyItem ───────────────────────────────────────────────────────────────────
  @Nested
  class BuyItem {

    //    @BeforeEach
    //    void givenSwordIsAvailable() {
    //      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.of(sword));
    //    }

    @Test
    void shouldCallAvatarPortInOrder() {
      givenMarketplaceExistsWithAvatar();
      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      commandService.buyItem(MARKETPLACE_MP_ID, SWORD_NAME, LEVEL_10);
      InOrder order = inOrder(avatarPort, marketplace, marketplaceRepository);
      order.verify(avatarPort).spendMoney(eq(AVATAR_ID_99.value()), any());
      order.verify(avatarPort).addItemToInventory(eq(AVATAR_ID_99.value()), eq(sword));
      order.verify(marketplace).buyItem(SWORD_NAME);
      order.verify(marketplaceRepository).save(marketplace);
    }

    @Test
    void shouldSaveMarketplaceAfterBuy() {
      givenMarketplaceExistsWithAvatar();
      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      commandService.buyItem(MARKETPLACE_MP_ID, SWORD_NAME, LEVEL_10);
      verify(marketplaceRepository).save(marketplace);
    }

    @Test
    void shouldPublishItemBoughtEvent() {
      givenMarketplaceExistsWithAvatar();
      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      commandService.buyItem(MARKETPLACE_MP_ID, SWORD_NAME, LEVEL_10);
      ArgumentCaptor<ItemBought> captor = ArgumentCaptor.forClass(ItemBought.class);
      verify(marketplaceObserver).notifyMarketplaceEvent(captor.capture());
      ItemBought event = captor.getValue();
      assertThat(event.marketplaceId()).isEqualTo(MARKETPLACE_MP_ID);
      assertThat(event.itemName()).isEqualTo(SWORD_NAME);
      assertThat(event.avatarId()).isEqualTo(AVATAR_ID_99);
    }

    @Test
    void shouldThrowInsufficientLevelWhenLevelTooLow() {
      givenMarketplaceExistsWithAvatar();
      when(marketplace.getAvailableItem(ELITE_SWORD)).thenReturn(Optional.of(eliteSword));
      assertThatThrownBy(() -> commandService.buyItem(MARKETPLACE_MP_ID, ELITE_SWORD, new Level(1)))
          .isInstanceOf(InsufficientLevelException.class);
      verifyNoInteractions(avatarPort);
    }

    @Test
    void shouldCompensateWithEarnMoneyWhenAddItemFails() {
      givenMarketplaceExistsWithAvatar();
      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      doThrow(new AvatarCommunicationException("fail", null))
          .when(avatarPort)
          .addItemToInventory(any(), any());

      assertThatThrownBy(() -> commandService.buyItem(MARKETPLACE_MP_ID, SWORD_NAME, LEVEL_10))
          .isInstanceOf(AvatarCommunicationException.class);

      verify(avatarPort).spendMoney(any(), any());
      verify(avatarPort).earnMoney(eq(AVATAR_ID_99.value()), eq(sword.price())); // rollback
      verify(marketplaceRepository, never()).save(any());
    }

    @Test
    void shouldCompensateFullyWhenBuyItemDomainFails() {
      givenMarketplaceExistsWithAvatar();
      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      doThrow(new AvatarCommunicationException("domain error", null))
          .when(marketplace)
          .buyItem(SWORD_NAME);

      assertThatThrownBy(() -> commandService.buyItem(MARKETPLACE_MP_ID, SWORD_NAME, LEVEL_10))
          .isInstanceOf(AvatarCommunicationException.class);

      verify(avatarPort).removeItemFromInventory(eq(AVATAR_ID_99.value()), eq(sword));
      verify(avatarPort).earnMoney(eq(AVATAR_ID_99.value()), eq(sword.price()));
      verify(marketplaceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      assertThatThrownBy(() -> commandService.buyItem(MISSING_MARKETPLACE_ID, SWORD_NAME, LEVEL_10))
          .isInstanceOf(MarketplaceNotFoundException.class);
      verifyNoInteractions(avatarPort);
    }

    @Test
    void shouldNeverPublishEventWhenMarketplaceMissing() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      ignoreThrows(() -> commandService.buyItem(MISSING_MARKETPLACE_ID, SWORD_NAME, LEVEL_10));
      verifyNoInteractions(marketplaceObserver);
    }
  }

  // ── SellItem ──────────────────────────────────────────────────────────────────
  @Nested
  class SellItem {

    //    @BeforeEach
    //    void givenSwordIsSold() {
    //      when(marketplace.getSoldItem(SWORD_NAME)).thenReturn(Optional.of(sword));
    //    }

    @Test
    void shouldCallAvatarPortInOrder() {
      givenMarketplaceExistsWithAvatar();
      when(marketplace.getSoldItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      commandService.sellItem(MARKETPLACE_MP_ID, SWORD_NAME);
      InOrder order = inOrder(avatarPort, marketplace, marketplaceRepository);
      order.verify(avatarPort).removeItemFromInventory(eq(AVATAR_ID_99.value()), eq(sword));
      order.verify(avatarPort).earnMoney(eq(AVATAR_ID_99.value()), any());
      order.verify(marketplace).sellItem(SWORD_NAME);
      order.verify(marketplaceRepository).save(marketplace);
    }

    @Test
    void shouldSaveMarketplaceAfterSell() {
      givenMarketplaceExistsWithAvatar();
      when(marketplace.getSoldItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      commandService.sellItem(MARKETPLACE_MP_ID, SWORD_NAME);
      verify(marketplaceRepository).save(marketplace);
    }

    @Test
    void shouldPublishItemSoldEvent() {
      givenMarketplaceExistsWithAvatar();
      when(marketplace.getSoldItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      commandService.sellItem(MARKETPLACE_MP_ID, SWORD_NAME);
      ArgumentCaptor<ItemSold> captor = ArgumentCaptor.forClass(ItemSold.class);
      verify(marketplaceObserver).notifyMarketplaceEvent(captor.capture());
      ItemSold event = captor.getValue();
      assertThat(event.marketplaceId()).isEqualTo(MARKETPLACE_MP_ID);
      assertThat(event.itemName()).isEqualTo(SWORD_NAME);
      assertThat(event.avatarId()).isEqualTo(AVATAR_ID_99);
    }

    @Test
    void shouldCompensateWithAddItemWhenEarnMoneyFails() {
      givenMarketplaceExistsWithAvatar();
      when(marketplace.getSoldItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      doThrow(new AvatarCommunicationException("fail", null))
          .when(avatarPort)
          .earnMoney(any(), any());

      assertThatThrownBy(() -> commandService.sellItem(MARKETPLACE_MP_ID, SWORD_NAME))
          .isInstanceOf(AvatarCommunicationException.class);

      verify(avatarPort).removeItemFromInventory(any(), any());
      verify(avatarPort).addItemToInventory(eq(AVATAR_ID_99.value()), eq(sword));
      verify(marketplaceRepository, never()).save(any());
    }

    @Test
    void shouldCompensateFullyWhenSellItemDomainFails() {
      givenMarketplaceExistsWithAvatar();
      when(marketplace.getSoldItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      doThrow(new AvatarCommunicationException("domain error", null))
          .when(marketplace)
          .sellItem(SWORD_NAME);

      assertThatThrownBy(() -> commandService.sellItem(MARKETPLACE_MP_ID, SWORD_NAME))
          .isInstanceOf(AvatarCommunicationException.class);

      verify(avatarPort).spendMoney(eq(AVATAR_ID_99.value()), eq(sword.price()));
      verify(avatarPort).addItemToInventory(eq(AVATAR_ID_99.value()), eq(sword));
      verify(marketplaceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      assertThatThrownBy(() -> commandService.sellItem(MISSING_MARKETPLACE_ID, SWORD_NAME))
          .isInstanceOf(MarketplaceNotFoundException.class);
      verifyNoInteractions(avatarPort);
    }

    @Test
    void shouldNeverPublishEventWhenMarketplaceMissing() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      ignoreThrows(() -> commandService.sellItem(MISSING_MARKETPLACE_ID, SWORD_NAME));
      verifyNoInteractions(marketplaceObserver);
    }
  }

  // ── canBuyItem ────────────────────────────────────────────────────────────────

  @Nested
  class CanBuyItem {

    @Test
    void shouldReturnTrueWhenPlayerLevelMeetsRequirement() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      assertThat(queryService.canBuyItem(MARKETPLACE_MP_ID, SWORD_NAME, LEVEL_1)).isTrue();
    }

    @Test
    void shouldReturnTrueWhenPlayerLevelExceedsRequirement() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      assertThat(queryService.canBuyItem(MARKETPLACE_MP_ID, SWORD_NAME, LEVEL_10)).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPlayerLevelBelowRequirement() {
      givenMarketplaceExists();
      // high-level item requires level 10, player is only level 1
      when(marketplace.getAvailableItem("Elite Sword")).thenReturn(Optional.of(eliteSword()));
      assertThat(queryService.canBuyItem(MARKETPLACE_MP_ID, "Elite Sword", LEVEL_1)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenPlayerLevelIsOneBelow() {
      givenMarketplaceExists();
      // item requires level 5, player is level 4 — boundary case
      when(marketplace.getAvailableItem("Mid Sword")).thenReturn(Optional.of(midSword()));
      assertThat(queryService.canBuyItem(MARKETPLACE_MP_ID, "Mid Sword", new Level(4))).isFalse();
    }

    @Test
    void shouldThrowItemNotFoundWhenItemDoesNotExist() {
      givenMarketplaceExists();
      when(marketplace.getAvailableItem(UNKNOWN_ITEM_NAME)).thenReturn(Optional.empty());
      assertThatThrownBy(
              () -> queryService.canBuyItem(MARKETPLACE_MP_ID, UNKNOWN_ITEM_NAME, LEVEL_1))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowMarketplaceNotFoundWhenMarketplaceMissing() {
      givenMarketplaceNotFound(MISSING_MARKETPLACE_ID);
      assertThatThrownBy(() -> queryService.canBuyItem(MISSING_MARKETPLACE_ID, SWORD_NAME, LEVEL_1))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── Observer delegation ───────────────────────────────────────────────────────

  @Nested
  class ObserverDelegation {

    @Test
    void buyAndSellShouldEachPublishExactlyOneEvent() {
      givenMarketplaceExistsWithAvatar();
      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      when(marketplace.getSoldItem(SWORD_NAME)).thenReturn(Optional.of(sword));

      commandService.buyItem(MARKETPLACE_MP_ID, SWORD_NAME, LEVEL_10);
      commandService.sellItem(MARKETPLACE_MP_ID, SWORD_NAME);

      verify(marketplaceObserver, times(2)).notifyMarketplaceEvent(any());
    }

    @Test
    void buyEventAndSellEventShouldHaveDifferentTypes() {
      givenMarketplaceExistsWithAvatar();
      when(marketplace.getAvailableItem(SWORD_NAME)).thenReturn(Optional.of(sword));
      when(marketplace.getSoldItem(SWORD_NAME)).thenReturn(Optional.of(sword));

      commandService.buyItem(MARKETPLACE_MP_ID, SWORD_NAME, LEVEL_10);
      commandService.sellItem(MARKETPLACE_MP_ID, SWORD_NAME);

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
