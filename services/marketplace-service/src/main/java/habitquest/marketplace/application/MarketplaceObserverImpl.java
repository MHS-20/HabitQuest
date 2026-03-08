package habitquest.marketplace.application;

import habitquest.marketplace.domain.events.ItemBought;
import habitquest.marketplace.domain.events.ItemSold;
import habitquest.marketplace.domain.events.MarketplaceEvent;
import habitquest.marketplace.domain.events.MarketplaceObserver;
import org.springframework.stereotype.Component;

@Component
public class MarketplaceObserverImpl implements MarketplaceObserver {

  private final MarketplaceNotifier marketplaceNotifier;

  public MarketplaceObserverImpl(MarketplaceNotifier marketplaceNotifier) {
    this.marketplaceNotifier = marketplaceNotifier;
  }

  @Override
  public void notifyMarketplaceEvent(MarketplaceEvent event) {
    switch (event) {
      case ItemBought e -> marketplaceNotifier.notifyItemBought(e);
      case ItemSold e -> marketplaceNotifier.notifyItemSold(e);
      default ->
          throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
    }
  }
}
