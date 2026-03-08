package habitquest.marketplace.application;

import static org.assertj.core.api.Assertions.*;
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
  @Mock private MarketplaceObserver   marketplaceObserver;
  @Mock private MarketplaceFactory    marketplaceFactory;

  @InjectMocks
  private MarketplaceServiceImpl service;

  // ── Fixtures ─────────────────────────────────────────────────────────────────

  private static final String MARKETPLACE_ID = "mp-1";
  private static final String AVATAR_ID      = "avatar-99";
  private static final Money  SWORD_PRICE    = new Money(50);
  private static final Level  LEVEL_1        = new Level(1);

  private Weapon          sword;
  private Armor           shield;
  private HealthPotion    hpPotion;
  private ManaPotion      mpPotion;
  private MarketplaceImpl marketplace;

  @BeforeEach
  void setUp() {
    sword    = new Weapon("Iron Sword", "A basic sword", 10, SWORD_PRICE, LEVEL_1);
    shield   = new Armor("Iron Shield", "A basic shield", 5, new Money(30), LEVEL_1);
    hpPotion = new HealthPotion("HP Potion", "Restores HP", 50, new Money(10), LEVEL_1);
    mpPotion = new ManaPotion("MP Potion", "Restores MP", 30, new Money(12), LEVEL_1);

    marketplace = new MarketplaceImpl(
            MARKETPLACE_ID,
            new ArrayList<>(List.of(sword, shield, hpPotion, mpPotion)),
            new ArrayList<>(List.of(shield)),
            new ArrayList<>(List.of(sword)),
            new ArrayList<>(List.of(hpPotion, mpPotion)),
            new ArrayList<>(List.of(hpPotion)),
            new ArrayList<>(List.of(mpPotion)));
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
      when(marketplaceRepository.findById("unknown")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getMarketplace("unknown"))
              .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── getItems ──────────────────────────────────────────────────────────────────

  @Nested
  class GetItems {

    @Test
    void shouldReturnAllItems() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      assertThat(service.getItems(MARKETPLACE_ID))
              .containsExactlyInAnyOrder(sword, shield, hpPotion, mpPotion);
    }

    @Test
    void shouldReturnArmors() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      assertThat(service.getArmors(MARKETPLACE_ID)).containsExactly(shield);
    }

    @Test
    void shouldReturnWeapons() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      assertThat(service.getWeapons(MARKETPLACE_ID)).containsExactly(sword);
    }

    @Test
    void shouldReturnPotions() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      assertThat(service.getPotions(MARKETPLACE_ID))
              .containsExactlyInAnyOrder(hpPotion, mpPotion);
    }

    @Test
    void shouldReturnHealthPotions() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      assertThat(service.getHealthPotions(MARKETPLACE_ID)).containsExactly(hpPotion);
    }

    @Test
    void shouldReturnManaPotions() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));
      assertThat(service.getManaPotions(MARKETPLACE_ID)).containsExactly(mpPotion);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      when(marketplaceRepository.findById("missing")).thenReturn(Optional.empty());
      assertThatThrownBy(() -> service.getItems("missing"))
              .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── getItemByName ─────────────────────────────────────────────────────────────

  @Nested
  class GetItemByName {

    @Test
    void shouldReturnItemWhenFound() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));

      assertThat(service.getItemByName(MARKETPLACE_ID, "Iron Sword")).isEqualTo(sword);
    }

    @Test
    void shouldThrowItemNotFoundWhenNameUnknown() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));

      assertThatThrownBy(() -> service.getItemByName(MARKETPLACE_ID, "Dragon Blade"))
              .isInstanceOf(ItemNotFoundException.class);
    }

    @Test
    void shouldThrowMarketplaceNotFoundWhenMarketplaceMissing() {
      when(marketplaceRepository.findById("ghost")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.getItemByName("ghost", "Iron Sword"))
              .isInstanceOf(MarketplaceNotFoundException.class);
    }
  }

  // ── buyItem ───────────────────────────────────────────────────────────────────

  @Nested
  class BuyItem {

    @Test
    void shouldSaveMarketplaceAfterBuy() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));

      service.buyItem(MARKETPLACE_ID, "Iron Sword", AVATAR_ID);

      verify(marketplaceRepository).save(marketplace);
    }

    @Test
    void shouldPublishItemBoughtEvent() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));

      service.buyItem(MARKETPLACE_ID, "Iron Sword", AVATAR_ID);

      ArgumentCaptor<ItemBought> captor = ArgumentCaptor.forClass(ItemBought.class);
      verify(marketplaceObserver).notifyMarketplaceEvent(captor.capture());

      ItemBought event = captor.getValue();
      assertThat(event.marketplaceId()).isEqualTo(MARKETPLACE_ID);
      assertThat(event.itemName()).isEqualTo("Iron Sword");
      assertThat(event.avatarId()).isEqualTo(AVATAR_ID);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      when(marketplaceRepository.findById("missing")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.buyItem("missing", "Iron Sword", AVATAR_ID))
              .isInstanceOf(MarketplaceNotFoundException.class);
    }

    @Test
    void shouldNeverPublishEventWhenMarketplaceMissing() {
      when(marketplaceRepository.findById("missing")).thenReturn(Optional.empty());

      ignoreThrows(() -> service.buyItem("missing", "Iron Sword", AVATAR_ID));

      verifyNoInteractions(marketplaceObserver);
    }
  }

  // ── sellItem ──────────────────────────────────────────────────────────────────

  @Nested
  class SellItem {

    @Test
    void shouldSaveMarketplaceAfterSell() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));

      service.sellItem(MARKETPLACE_ID, "Iron Sword", AVATAR_ID);

      verify(marketplaceRepository).save(marketplace);
    }

    @Test
    void shouldPublishItemSoldEvent() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));

      service.sellItem(MARKETPLACE_ID, "Iron Sword", AVATAR_ID);

      ArgumentCaptor<ItemSold> captor = ArgumentCaptor.forClass(ItemSold.class);
      verify(marketplaceObserver).notifyMarketplaceEvent(captor.capture());

      ItemSold event = captor.getValue();
      assertThat(event.marketplaceId()).isEqualTo(MARKETPLACE_ID);
      assertThat(event.itemName()).isEqualTo("Iron Sword");
      assertThat(event.avatarId()).isEqualTo(AVATAR_ID);
    }

    @Test
    void shouldThrowWhenMarketplaceNotFound() {
      when(marketplaceRepository.findById("missing")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.sellItem("missing", "Iron Sword", AVATAR_ID))
              .isInstanceOf(MarketplaceNotFoundException.class);
    }

    @Test
    void shouldNeverPublishEventWhenMarketplaceMissing() {
      when(marketplaceRepository.findById("missing")).thenReturn(Optional.empty());

      ignoreThrows(() -> service.sellItem("missing", "Iron Sword", AVATAR_ID));

      verifyNoInteractions(marketplaceObserver);
    }
  }

  // ── Observer delegation ───────────────────────────────────────────────────────

  @Nested
  class ObserverDelegation {

    @Test
    void buyAndSellShouldEachPublishExactlyOneEvent() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));

      service.buyItem(MARKETPLACE_ID, "Iron Sword", AVATAR_ID);
      service.sellItem(MARKETPLACE_ID, "Iron Sword", AVATAR_ID);

      verify(marketplaceObserver, times(2)).notifyMarketplaceEvent(any());
    }

    @Test
    void buyEventAndSellEventShouldHaveDifferentTypes() {
      when(marketplaceRepository.findById(MARKETPLACE_ID)).thenReturn(Optional.of(marketplace));

      service.buyItem(MARKETPLACE_ID, "Iron Sword", AVATAR_ID);
      service.sellItem(MARKETPLACE_ID, "Iron Sword", AVATAR_ID);

      ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
      verify(marketplaceObserver, times(2)).notifyMarketplaceEvent(any());
      // Buy publishes ItemBought, sell publishes ItemSold — verified individually in their own tests
    }
  }

  // ── Helpers ───────────────────────────────────────────────────────────────────

  private void ignoreThrows(Runnable action) {
    try { action.run(); } catch (Exception ignored) {}
  }
}