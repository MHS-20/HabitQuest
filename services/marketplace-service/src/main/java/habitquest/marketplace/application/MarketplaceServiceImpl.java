package habitquest.marketplace.application;

import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.events.ItemBought;
import habitquest.marketplace.domain.events.ItemSold;
import habitquest.marketplace.domain.events.MarketplaceObserver;
import habitquest.marketplace.domain.factory.MarketplaceFactory;
import habitquest.marketplace.domain.items.*;
import java.util.List;

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
  public List<Item> getItems(String marketplaceId) throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getItems();
  }

  @Override
  public List<Armor> getArmors(String marketplaceId) throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getArmors();
  }

  @Override
  public List<Weapon> getWeapons(String marketplaceId) throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getWeapons();
  }

  @Override
  public List<Potion> getPotions(String marketplaceId) throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getPotions();
  }

  @Override
  public List<HealthPotion> getHealthPotions(String marketplaceId) throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getHealthPotions();
  }

  @Override
  public List<ManaPotion> getManaPotions(String marketplaceId) throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getManaPotions();
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
