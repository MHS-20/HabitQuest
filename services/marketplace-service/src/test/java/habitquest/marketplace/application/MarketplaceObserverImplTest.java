package habitquest.marketplace.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import habitquest.marketplace.domain.events.ItemBought;
import habitquest.marketplace.domain.events.ItemSold;
import habitquest.marketplace.domain.events.MarketplaceEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarketplaceObserverImplTest {

  @Mock private MarketplaceNotifier notifier;
  @InjectMocks private MarketplaceObserverImpl observer;

  private static final ItemBought ITEM_BOUGHT = new ItemBought("mp-1", "Iron Sword", "avatar-1");
  private static final ItemSold ITEM_SOLD = new ItemSold("mp-1", "Iron Sword", "avatar-1");

  @Test
  void shouldDelegateItemBoughtToNotifier() {
    observer.notifyMarketplaceEvent(ITEM_BOUGHT);

    verify(notifier).notifyItemBought(ITEM_BOUGHT);
    verifyNoMoreInteractions(notifier);
  }

  @Test
  void shouldDelegateItemSoldToNotifier() {
    observer.notifyMarketplaceEvent(ITEM_SOLD);

    verify(notifier).notifyItemSold(ITEM_SOLD);
    verifyNoMoreInteractions(notifier);
  }

  @Test
  void shouldThrowForUnknownEventType() {
    MarketplaceEvent unknown = new MarketplaceEvent() {};

    assertThatThrownBy(() -> observer.notifyMarketplaceEvent(unknown))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown event type");
  }
}
