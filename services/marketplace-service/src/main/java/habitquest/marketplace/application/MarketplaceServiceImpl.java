package habitquest.marketplace.application;

import habitquest.marketplace.domain.ItemNotFoundException;
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
  public Marketplace getMarketplace(String marketplaceId) throws MarketplaceNotFoundException {
    return marketplaceRepository
        .findById(marketplaceId)
        .orElseThrow(() -> new MarketplaceNotFoundException(marketplaceId));
  }

  @Override
  public String createMarketplaceForAvatar(String avatarId) {
    Marketplace marketplace = marketplaceFactory.create(avatarId);
    marketplaceRepository.save(marketplace);
    return marketplace.getId();
  }

  @Override
  public String getAvatarId(String marketplaceId) throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getAvatarId();
  }

  @Override
  public List<Item> getAllAvailableItems(String marketplaceId) throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getAllAvailableItems();
  }

  @Override
  public List<Item> getAvailableItemsByType(String marketplaceId, ItemType type)
      throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getAvailableItemsByType(type);
  }

  @Override
  public Item getAvailableItem(String marketplaceId, String itemName)
      throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId)
        .getAvailableItem(itemName)
        .orElseThrow(() -> new ItemNotFoundException(itemName));
  }

  @Override
  public List<Item> getSoldItems(String marketplaceId) throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getSoldItems();
  }

  @Override
  public Item getSoldItem(String marketplaceId, String itemName)
      throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId)
        .getSoldItem(itemName)
        .orElseThrow(() -> new ItemNotFoundException(itemName));
  }

  // -------------------------------------------------
  // Commands
  // -------------------------------------------------
  @Override
  public void buyItem(String marketplaceId, String itemName) throws MarketplaceNotFoundException {
    Marketplace marketplace = getMarketplace(marketplaceId);
    marketplace.buyItem(itemName);
    marketplaceRepository.save(marketplace);
    marketplaceObserver.notifyMarketplaceEvent(
        new ItemBought(marketplaceId, itemName, marketplace.getAvatarId()));
  }

  @Override
  public void sellItem(String marketplaceId, String itemName) throws MarketplaceNotFoundException {
    Marketplace marketplace = getMarketplace(marketplaceId);
    marketplace.sellItem(itemName);
    marketplaceRepository.save(marketplace);
    marketplaceObserver.notifyMarketplaceEvent(
        new ItemSold(marketplaceId, itemName, marketplace.getAvatarId()));
  }
}
