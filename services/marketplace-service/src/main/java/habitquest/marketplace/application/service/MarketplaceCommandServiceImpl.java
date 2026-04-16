package habitquest.marketplace.application.service;

import common.ddd.Id;
import habitquest.marketplace.application.exceptions.AvatarCommunicationException;
import habitquest.marketplace.application.exceptions.InsufficientLevelException;
import habitquest.marketplace.application.exceptions.MarketplaceNotFoundException;
import habitquest.marketplace.application.port.in.MarketplaceCommandService;
import habitquest.marketplace.application.port.in.MarketplaceQueryService;
import habitquest.marketplace.application.port.out.AvatarClientPort;
import habitquest.marketplace.application.port.out.MarketplaceRepository;
import habitquest.marketplace.domain.events.ItemBought;
import habitquest.marketplace.domain.events.ItemSold;
import habitquest.marketplace.domain.events.MarketplaceObserver;
import habitquest.marketplace.domain.exceptions.ItemNotFoundException;
import habitquest.marketplace.domain.factory.MarketplaceFactory;
import habitquest.marketplace.domain.items.Item;
import habitquest.marketplace.domain.items.Level;
import habitquest.marketplace.domain.marketplace.Avatar;
import habitquest.marketplace.domain.marketplace.Marketplace;
import habitquest.marketplace.domain.marketplace.Money;
import org.springframework.stereotype.Service;

@Service
public class MarketplaceCommandServiceImpl implements MarketplaceCommandService {

  private final MarketplaceRepository marketplaceRepository;
  private final MarketplaceObserver marketplaceObserver;
  private final MarketplaceFactory marketplaceFactory;
  private final AvatarClientPort avatarPort;
  private final MarketplaceQueryService queryService;

  public MarketplaceCommandServiceImpl(
      MarketplaceRepository marketplaceRepository,
      MarketplaceObserver marketplaceObserver,
      MarketplaceFactory marketplaceFactory,
      AvatarClientPort avatarPort,
      MarketplaceQueryService queryService) {
    this.marketplaceRepository = marketplaceRepository;
    this.marketplaceObserver = marketplaceObserver;
    this.marketplaceFactory = marketplaceFactory;
    this.avatarPort = avatarPort;
    this.queryService = queryService;
  }

  @Override
  public Id<Marketplace> createMarketplaceForAvatar(Id<Avatar> avatarId) {
    Marketplace marketplace = marketplaceFactory.create(avatarId);
    marketplaceRepository.save(marketplace);
    return marketplace.getId();
  }

  @Override
  public void buyItem(Id<Marketplace> marketplaceId, String itemName, Level currentLevel) {
    Marketplace marketplace = queryService.getMarketplace(marketplaceId);
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
    Marketplace marketplace = queryService.getMarketplace(marketplaceId);
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
}
