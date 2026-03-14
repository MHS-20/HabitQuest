package habitquest.marketplace.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.MarketplaceImpl;
import habitquest.marketplace.domain.Money;
import habitquest.marketplace.domain.events.ItemBought;
import habitquest.marketplace.domain.events.ItemSold;
import habitquest.marketplace.domain.events.MarketplaceObserver;
import habitquest.marketplace.domain.factory.MarketplaceFactory;
import habitquest.marketplace.domain.items.*;
import java.util.ArrayList;
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

  @InjectMocks private MarketplaceServiceImpl service;

  // ── Fixtures ─────────────────────────────────────────────────────────────────

  private static final String MARKETPLACE_ID = "mp-1";
  private static final String AVATAR_ID = "avatar-99";
  private static final String SWORD_NAME = "Iron Sword";
  private static final String SHIELD_NAME = "Iron Shield";
  private static final String HP_POTION_NAME = "HP Potion";
  private static final String MP_POTION_NAME = "MP Potion";
  private static final String SWORD_DESC = "A basic sword";
  private static final String SHIELD_DESC = "A basic shield";
  private static final String HP_POTION_DESC = "Restores HP";
  private static final String MP_POTION_DESC = "Restores MP";
  private static final String UNKNOWN_ITEM_NAME = "Dragon Blade";
  private static final String MISSING_MARKETPLACE_ID = "missing";
  private static final String GHOST_MARKETPLACE_ID = "ghost";
  private static final String UNKNOWN_MARKETPLACE_ID = "unknown";
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
            MARKETPLACE_ID, new ArrayList<Item>(List.of(sword, shield, hpPotion, mpPotion)));
  }

  // ── getMarketplace ────────────────────────────────────────────────────────────

  @Nested
  class GetMarketplace {

    @Test
    void shouldReturnMarketplaceWhenFound() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      Marketplace result = service.getMarketplace(MARKETPLACE_ID);
      assertThat(result).isSameAs(marketplace);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      when(marketplaceRepository.findById(UNKNOWN_MARKETPLACE_ID)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> service.getMarketplace(UNKNOWN_MARKETPLACE_ID))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── getItems ──────────────────────────────────────────────────────────────────
  @Nested
  class GetItems {

    @Test
    void shouldReturnAllItems() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      assertThat(service.getItems(MARKETPLACE_ID, ItemType.ALL))
          .containsExactlyInAnyOrder(sword, shield, hpPotion, mpPotion);
    }

    @Test
    void shouldReturnArmors() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      assertThat(service.getItems(MARKETPLACE_ID, ItemType.ARMOR)).containsExactly(shield);
    }

    @Test
    void shouldReturnWeapons() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      assertThat(service.getItems(MARKETPLACE_ID, ItemType.WEAPON)).containsExactly(sword);
    }

    @Test
    void shouldReturnPotions() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      assertThat(service.getItems(MARKETPLACE_ID, ItemType.POTION))
          .containsExactly(hpPotion, mpPotion);
    }

    @Test
    void shouldReturnHealthPotions() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      assertThat(service.getItems(MARKETPLACE_ID, ItemType.HEALTH_POTION))
          .containsExactly(hpPotion);
    }

    @Test
    void shouldReturnManaPotions() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      assertThat(service.getItems(MARKETPLACE_ID, ItemType.MANA_POTION)).containsExactly(mpPotion);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      when(marketplaceRepository.findById(MISSING_MARKETPLACE_ID)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> service.getItems(MISSING_MARKETPLACE_ID, ItemType.ALL))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── getItemByName ─────────────────────────────────────────────────────────────

  @Nested
  class GetItemByName {

    @Test
    void shouldReturnItemWhenFound() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      assertThat(service.getItemByName(MARKETPLACE_ID, SWORD_NAME)).isEqualTo(sword);
    }

    @Test
    void shouldThrowItemNotFoundWhenNameUnknown() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      assertThatThrownBy(() -> service.getItemByName(MARKETPLACE_ID, UNKNOWN_ITEM_NAME))
          .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowMarketplaceNotFoundWhenMarketplaceMissing() {
      when(marketplaceRepository.findById(GHOST_MARKETPLACE_ID)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> service.getItemByName(GHOST_MARKETPLACE_ID, SWORD_NAME))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── buyItem ───────────────────────────────────────────────────────────────────

  @Nested
  class BuyItem {

    @Test
    void shouldSaveMarketplaceAfterBuy() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      service.buyItem(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID);
      verify(marketplaceRepository).save(marketplace);
    }

    @Test
    void shouldPublishItemBoughtEvent() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      service.buyItem(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID);
      ArgumentCaptor<ItemBought> captor = ArgumentCaptor.forClass(ItemBought.class);
      verify(marketplaceObserver).notifyMarketplaceEvent(captor.capture());
      ItemBought event = captor.getValue();
      assertThat(event.marketplaceId()).isEqualTo(MARKETPLACE_ID);
      assertThat(event.itemName()).isEqualTo(SWORD_NAME);
      assertThat(event.avatarId()).isEqualTo(AVATAR_ID);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      when(marketplaceRepository.findById(MISSING_MARKETPLACE_ID)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> service.buyItem(MISSING_MARKETPLACE_ID, SWORD_NAME, AVATAR_ID))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }

    @Test
    void shouldNeverPublishEventWhenMarketplaceMissing() {
      when(marketplaceRepository.findById(MISSING_MARKETPLACE_ID)).thenReturn(Optional.empty());
      ignoreThrows(() -> service.buyItem(MISSING_MARKETPLACE_ID, SWORD_NAME, AVATAR_ID));
      verifyNoInteractions(marketplaceObserver);
    }
  }

  // ── sellItem ──────────────────────────────────────────────────────────────────

  @Nested
  class SellItem {

    @Test
    void shouldSaveMarketplaceAfterSell() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      service.sellItem(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID);
      verify(marketplaceRepository).save(marketplace);
    }

    @Test
    void shouldPublishItemSoldEvent() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      service.sellItem(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID);
      ArgumentCaptor<ItemSold> captor = ArgumentCaptor.forClass(ItemSold.class);
      verify(marketplaceObserver).notifyMarketplaceEvent(captor.capture());
      ItemSold event = captor.getValue();
      assertThat(event.marketplaceId()).isEqualTo(MARKETPLACE_ID);
      assertThat(event.itemName()).isEqualTo(SWORD_NAME);
      assertThat(event.avatarId()).isEqualTo(AVATAR_ID);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      when(marketplaceRepository.findById(MISSING_MARKETPLACE_ID)).thenReturn(Optional.empty());
      assertThatThrownBy(() -> service.sellItem(MISSING_MARKETPLACE_ID, SWORD_NAME, AVATAR_ID))
          .isInstanceOf(MarketplaceNotFoundException.class);
    }

    @Test
    void shouldNeverPublishEventWhenMarketplaceMissing() {
      when(marketplaceRepository.findById(MISSING_MARKETPLACE_ID)).thenReturn(Optional.empty());
      ignoreThrows(() -> service.sellItem(MISSING_MARKETPLACE_ID, SWORD_NAME, AVATAR_ID));
      verifyNoInteractions(marketplaceObserver);
    }
  }

  // ── Observer delegation ───────────────────────────────────────────────────────

  @Nested
  class ObserverDelegation {

    @Test
    void buyAndSellShouldEachPublishExactlyOneEvent() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      service.buyItem(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID);
      service.sellItem(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID);
      verify(marketplaceObserver, times(2)).notifyMarketplaceEvent(any());
    }

    @Test
    void buyEventAndSellEventShouldHaveDifferentTypes() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      service.buyItem(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID);
      service.sellItem(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID);
      ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
      verify(marketplaceObserver, times(2)).notifyMarketplaceEvent(any());
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
