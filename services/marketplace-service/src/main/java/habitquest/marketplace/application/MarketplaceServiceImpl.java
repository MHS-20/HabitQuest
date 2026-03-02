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
  // Private utility
  // -------------------------------------------------

  private Marketplace loadMarketplace(String marketplaceId) {
    return marketplaceRepository
        .findById(marketplaceId)
        .orElseThrow(() -> new MarketplaceNotFoundException(marketplaceId));
  }

  // -------------------------------------------------
  // Queries
  // -------------------------------------------------

  @Override
  public Marketplace getMarketplace(String marketplaceId) {
    return loadMarketplace(marketplaceId);
  }

  @Override
  public List<Item> getItems(String marketplaceId) {
    return loadMarketplace(marketplaceId).getItems();
  }

  @Override
  public List<Armor> getArmors(String marketplaceId) {
    return loadMarketplace(marketplaceId).getArmors();
  }

  @Override
  public List<Weapon> getWeapons(String marketplaceId) {
    return loadMarketplace(marketplaceId).getWeapons();
  }

  @Override
  public List<Potion> getPotions(String marketplaceId) {
    return loadMarketplace(marketplaceId).getPotions();
  }

  @Override
  public List<HealthPotion> getHealthPotions(String marketplaceId) {
    return loadMarketplace(marketplaceId).getHealthPotions();
  }

  @Override
  public List<ManaPotion> getManaPotions(String marketplaceId) {
    return loadMarketplace(marketplaceId).getManaPotions();
  }

  @Override
  public Item getItemByName(String marketplaceId, String itemName) {
    return loadMarketplace(marketplaceId)
        .getItemByName(itemName)
        .orElseThrow(() -> new ItemNotFoundException(marketplaceId, itemName));
  }

  // -------------------------------------------------
  // Commands
  // -------------------------------------------------

  @Override
  public void buyItem(String marketplaceId, String itemName, String avatarId) {
    Marketplace marketplace = loadMarketplace(marketplaceId);
    marketplace.buyItem(itemName);
    marketplaceRepository.save(marketplace);
    marketplaceObserver.notifyMarketplaceEvent(new ItemBought(marketplaceId, itemName, avatarId));
  }

  @Override
  public void sellItem(String marketplaceId, String itemName, String avatarId) {
    Marketplace marketplace = loadMarketplace(marketplaceId);
    marketplace.sellItem(itemName);
    marketplaceRepository.save(marketplace);
    marketplaceObserver.notifyMarketplaceEvent(new ItemSold(marketplaceId, itemName, avatarId));
  }
}
