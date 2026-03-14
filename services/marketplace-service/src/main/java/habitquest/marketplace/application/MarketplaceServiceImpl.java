package habitquest.marketplace.application;

import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.events.ItemBought;
import habitquest.marketplace.domain.events.ItemSold;
import habitquest.marketplace.domain.events.MarketplaceObserver;
import habitquest.marketplace.domain.factory.MarketplaceFactory;
import habitquest.marketplace.domain.items.*;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MarketplaceServiceImpl implements MarketplaceService {
  private final MarketplaceRepository marketplaceRepository;
  private final MarketplaceObserver marketplaceObserver;
  private final MarketplaceFactory marketplaceFactory;

  public MarketplaceServiceImpl(
      MarketplaceRepository marketplaceRepository,
      MarketplaceObserver marketplaceObserver,
      MarketplaceFactory marketplaceFactory) {
    this.marketplaceRepository = marketplaceRepository;
    this.marketplaceObserver = marketplaceObserver;
    this.marketplaceFactory = marketplaceFactory;
  }

  // -------------------------------------------------
  // Queries
  // -------------------------------------------------

  @Override
  public Marketplace getMarketplace(String marketplaceId) {
    return marketplaceRepository
        .findById(marketplaceId)
        .orElseThrow(() -> new MarketplaceNotFoundException(marketplaceId));
  }

  @Override
  public List<Item> getItems(String marketplaceId, ItemType type)
      throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getItems(type);
  }

  @Override
  public Item getItemByName(String marketplaceId, String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException {
    return getMarketplace(marketplaceId)
        .getItem(itemName)
        .orElseThrow(() -> new ItemNotFoundException(marketplaceId, itemName));
  }

  // -------------------------------------------------
  // Commands
  // -------------------------------------------------

  @Override
  public void buyItem(String marketplaceId, String itemName, String avatarId)
      throws MarketplaceNotFoundException {
    Marketplace marketplace = getMarketplace(marketplaceId);
    marketplace.buyItem(itemName);
    marketplaceRepository.save(marketplace);
    marketplaceObserver.notifyMarketplaceEvent(new ItemBought(marketplaceId, itemName, avatarId));
  }

  @Override
  public void sellItem(String marketplaceId, String itemName, String avatarId)
      throws MarketplaceNotFoundException {
    Marketplace marketplace = getMarketplace(marketplaceId);
    marketplace.sellItem(itemName);
    marketplaceRepository.save(marketplace);
    marketplaceObserver.notifyMarketplaceEvent(new ItemSold(marketplaceId, itemName, avatarId));
  }
}
