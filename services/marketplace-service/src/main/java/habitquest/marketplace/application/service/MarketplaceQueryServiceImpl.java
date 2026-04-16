package habitquest.marketplace.application.service;

import common.ddd.Id;
import habitquest.marketplace.application.exceptions.MarketplaceNotFoundException;
import habitquest.marketplace.application.port.in.MarketplaceQueryService;
import habitquest.marketplace.application.port.out.MarketplaceRepository;
import habitquest.marketplace.domain.exceptions.ItemNotFoundException;
import habitquest.marketplace.domain.items.Item;
import habitquest.marketplace.domain.items.ItemFilter;
import habitquest.marketplace.domain.items.Level;
import habitquest.marketplace.domain.marketplace.Avatar;
import habitquest.marketplace.domain.marketplace.Marketplace;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MarketplaceQueryServiceImpl implements MarketplaceQueryService {

  private final MarketplaceRepository marketplaceRepository;

  public MarketplaceQueryServiceImpl(MarketplaceRepository marketplaceRepository) {
    this.marketplaceRepository = marketplaceRepository;
  }

  @Override
  public Marketplace getMarketplace(Id<Marketplace> marketplaceId)
      throws MarketplaceNotFoundException {
    return marketplaceRepository
        .findById(marketplaceId)
        .orElseThrow(() -> new MarketplaceNotFoundException(marketplaceId.value()));
  }

  @Override
  public Id<Marketplace> getMarketplaceIdByAvatarId(Id<Avatar> avatarId)
      throws MarketplaceNotFoundException {
    return marketplaceRepository
        .findByAvatarId(avatarId)
        .map(Marketplace::getId)
        .orElseThrow(
            () ->
                new MarketplaceNotFoundException(
                    "No marketplace found for avatar ID: " + avatarId.value()));
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
  public List<Item> getAvailableItemsByType(Id<Marketplace> marketplaceId, ItemFilter type)
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

  @Override
  public boolean canBuyItem(Id<Marketplace> marketplaceId, String itemName, Level level)
      throws MarketplaceNotFoundException {
    Item item =
        getMarketplace(marketplaceId)
            .getAvailableItem(itemName)
            .orElseThrow(() -> new ItemNotFoundException(itemName));
    return item.requiredLevel().levelNumber() <= level.levelNumber();
  }
}
