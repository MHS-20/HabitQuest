package habitquest.marketplace.application;

import common.ddd.Id;
import habitquest.marketplace.domain.Avatar;
import habitquest.marketplace.domain.ItemNotFoundException;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.Money;
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
  private final AvatarClientPort avatarPort;

  public MarketplaceServiceImpl(
      MarketplaceRepository marketplaceRepository,
      MarketplaceObserver marketplaceObserver,
      MarketplaceFactory marketplaceFactory,
      AvatarClientPort avatarPort) {
    this.marketplaceRepository = marketplaceRepository;
    this.marketplaceObserver = marketplaceObserver;
    this.marketplaceFactory = marketplaceFactory;
    this.avatarPort = avatarPort;
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
  public void buyItem(Id<Marketplace> marketplaceId, String itemName, Level currentLevel) {
    Marketplace marketplace = getMarketplace(marketplaceId);
    String avatarId = marketplace.getAvatarId().value();
    Item item =
        marketplace
            .getAvailableItem(itemName)
            .orElseThrow(() -> new ItemNotFoundException(itemName));

    if (item.requiredLevel().levelNumber() > currentLevel.levelNumber()) {
      throw new InsufficientLevelException(itemName);
    }

    Money price = item.price();
    boolean moneySpent = false;
    boolean inventoryAdded = false;
    try {
      avatarPort.spendMoney(avatarId, price);
      moneySpent = true;
      avatarPort.addItemToInventory(avatarId, item);
      inventoryAdded = true;
      marketplace.buyItem(itemName);
      marketplaceRepository.save(marketplace);
      marketplaceObserver.notifyMarketplaceEvent(
          new ItemBought(marketplaceId, itemName, marketplace.getAvatarId()));
    } catch (AvatarCommunicationException | IllegalStateException | ItemNotFoundException ex) {
      try {
        if (inventoryAdded) {
          avatarPort.removeItemFromInventory(avatarId, item);
        }
        if (moneySpent) {
          avatarPort.earnMoney(avatarId, price);
        }
      } catch (AvatarCommunicationException | ItemNotFoundException compensationEx) {
        throw new AvatarCommunicationException(
            "Partial failure and compensation failed during buy saga", compensationEx);
      }
      throw new AvatarCommunicationException(
          "Avatar operation failed during buy saga, compensation performed", ex);
    }
  }

  @Override
  public void sellItem(Id<Marketplace> marketplaceId, String itemName)
      throws MarketplaceNotFoundException {
    Marketplace marketplace = getMarketplace(marketplaceId);
    String avatarId = marketplace.getAvatarId().value();

    Item item =
        marketplace.getSoldItem(itemName).orElseThrow(() -> new ItemNotFoundException(itemName));
    Money price = item.price();

    boolean removedFromInventory = false;
    boolean earnedMoney = false;
    try {
      avatarPort.removeItemFromInventory(avatarId, item);
      removedFromInventory = true;

      avatarPort.earnMoney(avatarId, price);
      earnedMoney = true;

      marketplace.sellItem(itemName);
      marketplaceRepository.save(marketplace);
      marketplaceObserver.notifyMarketplaceEvent(
          new ItemSold(marketplaceId, itemName, marketplace.getAvatarId()));

    } catch (AvatarCommunicationException | ItemNotFoundException | IllegalArgumentException ex) {
      try {
        if (earnedMoney) {
          avatarPort.spendMoney(avatarId, price);
        }
        if (removedFromInventory) {
          avatarPort.addItemToInventory(avatarId, item);
        }
      } catch (AvatarCommunicationException | ItemNotFoundException compensationEx) {
        throw new AvatarCommunicationException(
            "Partial failure and compensation failed during sell saga", compensationEx);
      }
      throw new AvatarCommunicationException(
          "Avatar operation failed during sell saga, compensation performed", ex);
    }
  }

  @Override
  public boolean canBuyItem(Id<Marketplace> marketplaceId, String itemName, Level level)
      throws MarketplaceNotFoundException {
    Marketplace marketplace = getMarketplace(marketplaceId);
    Item item =
        marketplace
            .getAvailableItem(itemName)
            .orElseThrow(() -> new ItemNotFoundException(itemName));
    return item.requiredLevel().levelNumber() <= level.levelNumber();
  }
}
