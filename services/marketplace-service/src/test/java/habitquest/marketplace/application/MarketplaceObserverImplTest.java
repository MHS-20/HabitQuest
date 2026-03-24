package habitquest.marketplace.application;

import static habitquest.marketplace.MarketplaceFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import habitquest.marketplace.domain.events.MarketplaceEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarketplaceObserverImplTest {

  @Mock private MarketplaceNotifier notifier;
  @Mock private MarketplaceLogger log;
  @InjectMocks private MarketplaceObserverImpl observer;

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
