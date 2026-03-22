package habitquest.marketplace.application;

import common.ddd.Id;
import habitquest.marketplace.Avatar;
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
  public Marketplace getMarketplace(Id<Marketplace> marketplaceId)
      throws MarketplaceNotFoundException {
    return marketplaceRepository
        .findById(marketplaceId)
        .orElseThrow(() -> new MarketplaceNotFoundException(marketplaceId.value()));
  }

  @Override
  public Id<Marketplace> createMarketplaceForAvatar(Id<Avatar> avatarId) {
    Marketplace marketplace = marketplaceFactory.create(avatarId);
    marketplaceRepository.save(marketplace);
    return marketplace.getId();
  }

  @Override
  public Id<Avatar> getAvatarId(Id<Marketplace> marketplaceId) throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getAvatarId();
  }

  @Override
  public List<Item> getAllAvailableItems(Id<Marketplace> marketplaceId)
      throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getAllAvailableItems();
  }

  @Override
  public List<Item> getAvailableItemsByType(Id<Marketplace> marketplaceId, ItemType type)
      throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getAvailableItemsByType(type);
  }

  @Override
  public Item getAvailableItem(Id<Marketplace> marketplaceId, String itemName)
      throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId)
        .getAvailableItem(itemName)
        .orElseThrow(() -> new ItemNotFoundException(itemName));
  }

  @Override
  public List<Item> getSoldItems(Id<Marketplace> marketplaceId)
      throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId).getSoldItems();
  }

  @Override
  public Item getSoldItem(Id<Marketplace> marketplaceId, String itemName)
      throws MarketplaceNotFoundException {
    return getMarketplace(marketplaceId)
        .getSoldItem(itemName)
        .orElseThrow(() -> new ItemNotFoundException(itemName));
  }

  // -------------------------------------------------
  // Commands
  // -------------------------------------------------
  @Override
  public void buyItem(Id<Marketplace> marketplaceId, String itemName)
      throws MarketplaceNotFoundException {
    Marketplace marketplace = getMarketplace(marketplaceId);
    marketplace.buyItem(itemName);
    marketplaceRepository.save(marketplace);
    marketplaceObserver.notifyMarketplaceEvent(
        new ItemBought(marketplaceId, itemName, marketplace.getAvatarId()));
  }

  @Override
  public void sellItem(Id<Marketplace> marketplaceId, String itemName)
      throws MarketplaceNotFoundException {
    Marketplace marketplace = getMarketplace(marketplaceId);
    marketplace.sellItem(itemName);
    marketplaceRepository.save(marketplace);
    marketplaceObserver.notifyMarketplaceEvent(
        new ItemSold(marketplaceId, itemName, marketplace.getAvatarId()));
  }
}
