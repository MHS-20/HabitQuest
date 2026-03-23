package habitquest.marketplace.application;

import habitquest.marketplace.domain.events.ItemBought;
import habitquest.marketplace.domain.events.ItemSold;
import habitquest.marketplace.domain.events.MarketplaceEvent;
import habitquest.marketplace.domain.events.MarketplaceObserver;
import org.springframework.stereotype.Component;

@Component
public class MarketplaceObserverImpl implements MarketplaceObserver {

  private final MarketplaceNotifier marketplaceNotifier;
  private final MarketplaceLogger log;

  public MarketplaceObserverImpl(MarketplaceNotifier marketplaceNotifier, MarketplaceLogger log) {
    this.marketplaceNotifier = marketplaceNotifier;
    this.log = log;
  }

  @Override
  public void notifyMarketplaceEvent(MarketplaceEvent event) {
    log.info(event, "Received marketplace event");
    switch (event) {
      case ItemBought e -> handleItemBought(e);
      case ItemSold e -> handleItemSold(e);
      default -> {
        log.warn(event, "Unknown event type received");
        throw new IllegalArgumentException("Unknown event type: " + event.getClass().getName());
      }
    }
  }

  public void handleItemBought(ItemBought e) {
    log.info(e, "Handling ItemBought event");
    marketplaceNotifier.notifyItemBought(e);
  }

  public void handleItemSold(ItemSold e) {
    log.info(e, "Handling ItemSold event");
    marketplaceNotifier.notifyItemSold(e);
  }
}
