package habitquest.marketplace.domain.events;

public interface MarketplaceObserver {
  void notifyMarketplaceEvent(MarketplaceEvent event);
}
