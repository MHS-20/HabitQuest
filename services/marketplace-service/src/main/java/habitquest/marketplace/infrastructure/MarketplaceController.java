package habitquest.marketplace.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import common.ddd.Id;
import habitquest.marketplace.application.AvatarNotFoundException;
import habitquest.marketplace.application.MarketplaceLogger;
import habitquest.marketplace.application.MarketplaceNotFoundException;
import habitquest.marketplace.application.MarketplaceService;
import habitquest.marketplace.domain.Avatar;
import habitquest.marketplace.domain.ItemNotFoundException;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.Money;
import habitquest.marketplace.domain.items.*;
import habitquest.marketplace.infrastructure.dto.ItemMapper;
import habitquest.marketplace.infrastructure.dto.ItemResponse;
import habitquest.marketplace.infrastructure.dto.MarketplaceResponse;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/api/v1/marketplaces")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class MarketplaceController {

  private final MarketplaceService marketplaceService;
  private final AvatarClient avatarClient;
  private final MarketplaceLogger log;

  public MarketplaceController(
      MarketplaceService marketplaceService, AvatarClient avatarClient, MarketplaceLogger log) {
    this.marketplaceService = marketplaceService;
    this.avatarClient = avatarClient;
    this.log = log;
  }

  private Id<Marketplace> idOfMarketplace(String marketplaceId) {
    return new Id<>(marketplaceId);
  }

  private Id<Avatar> idOfAvatar(String avatarId) {
    return new Id<>(avatarId);
  }

  // ─── Marketplace ────────────────────────────────────────────────────────────

  @GetMapping("/{marketplaceId}")
  public ResponseEntity<EntityModel<MarketplaceResponse>> getMarketplace(
      @PathVariable String marketplaceId) {
    Marketplace marketplace = marketplaceService.getMarketplace(idOfMarketplace(marketplaceId));
    log.info(marketplace, "Fetched marketplace");
    MarketplaceResponse dto = MarketplaceResponse.from(marketplace);
    EntityModel<MarketplaceResponse> model =
        EntityModel.of(
            dto,
            selfMarketplaceLink(marketplaceId),
            linkTo(
                    methodOn(MarketplaceController.class)
                        .getAvailableItems(marketplaceId, ItemType.ALL))
                .withRel("items"),
            linkTo(methodOn(MarketplaceController.class).getSoldItems(marketplaceId))
                .withRel("sold-items"));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/by-avatar/{avatarId}")
  public ResponseEntity<EntityModel<MarketplaceResponse>> getMarketplaceByAvatarId(
      @PathVariable String avatarId) {
    Id<Avatar> avatarIdObj = idOfAvatar(avatarId);
    Id<Marketplace> marketplaceId = marketplaceService.getMarketplaceIdByAvatarId(avatarIdObj);
    return getMarketplace(marketplaceId.value());
  }

  @PostMapping
  public ResponseEntity<EntityModel<MarketplaceResponse>> createMarketplace(
      @RequestBody CreateMarketplaceRequest request) throws AvatarCommunicationException {
    log.info(request, "Creating marketplace");

    String marketplaceId =
        marketplaceService.createMarketplaceForAvatar(idOfAvatar(request.avatarId())).value();

    Marketplace marketplace = marketplaceService.getMarketplace(idOfMarketplace(marketplaceId));
    log.info(marketplace, "Marketplace created");

    MarketplaceResponse dto = MarketplaceResponse.from(marketplace);
    EntityModel<MarketplaceResponse> model =
        EntityModel.of(
            dto,
            selfMarketplaceLink(marketplaceId),
            linkTo(
                    methodOn(MarketplaceController.class)
                        .getAvailableItems(marketplaceId, ItemType.ALL))
                .withRel("items"),
            linkTo(methodOn(MarketplaceController.class).getSoldItems(marketplaceId))
                .withRel("sold-items"));

    return ResponseEntity.created(selfMarketplaceLink(marketplaceId).toUri()).body(model);
  }

  // ─── Available Items ────────────────────────────────────────────────────────

  @GetMapping("/{marketplaceId}/items")
  public ResponseEntity<CollectionModel<EntityModel<ItemResponse>>> getAvailableItems(
      @PathVariable String marketplaceId, @RequestParam(defaultValue = "ALL") ItemType type)
      throws MarketplaceNotFoundException {

    List<Item> items =
        type == ItemType.ALL
            ? marketplaceService.getAllAvailableItems(idOfMarketplace(marketplaceId))
            : marketplaceService.getAvailableItemsByType(idOfMarketplace(marketplaceId), type);

    log.info(items, "Fetched available items");

    List<EntityModel<ItemResponse>> itemModels =
        items.stream().map(item -> availableItemModel(marketplaceId, item)).toList();

    CollectionModel<EntityModel<ItemResponse>> model =
        CollectionModel.of(
            itemModels,
            linkTo(methodOn(MarketplaceController.class).getAvailableItems(marketplaceId, type))
                .withSelfRel(),
            selfMarketplaceLink(marketplaceId));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{marketplaceId}/items/{itemName}")
  public ResponseEntity<EntityModel<ItemResponse>> getAvailableItem(
      @PathVariable String marketplaceId, @PathVariable String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException {
    Item item = marketplaceService.getAvailableItem(idOfMarketplace(marketplaceId), itemName);
    log.info(item, "Fetched available item");
    return ResponseEntity.ok(availableItemModel(marketplaceId, item));
  }

  // ─── Sold Items ─────────────────────────────────────────────────────────────

  @GetMapping("/{marketplaceId}/sold-items")
  public ResponseEntity<CollectionModel<EntityModel<ItemResponse>>> getSoldItems(
      @PathVariable String marketplaceId) throws MarketplaceNotFoundException {

    List<Item> items = marketplaceService.getSoldItems(idOfMarketplace(marketplaceId));
    log.info(items, "Fetched sold items");

    List<EntityModel<ItemResponse>> itemModels =
        items.stream().map(item -> soldItemModel(marketplaceId, item)).toList();

    CollectionModel<EntityModel<ItemResponse>> model =
        CollectionModel.of(
            itemModels,
            linkTo(methodOn(MarketplaceController.class).getSoldItems(marketplaceId)).withSelfRel(),
            selfMarketplaceLink(marketplaceId));
    return ResponseEntity.ok(model);
  }

  @GetMapping("/{marketplaceId}/sold-items/{itemName}")
  public ResponseEntity<EntityModel<ItemResponse>> getSoldItem(
      @PathVariable String marketplaceId, @PathVariable String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException {
    Item item = marketplaceService.getSoldItem(idOfMarketplace(marketplaceId), itemName);
    log.info(item, "Fetched sold item");
    return ResponseEntity.ok(soldItemModel(marketplaceId, item));
  }

  // ─── Commands ───────────────────────────────────────────────────────────────

  @PostMapping("/{marketplaceId}/items/{itemName}/buy")
  public ResponseEntity<Void> buyItem(
      @PathVariable String marketplaceId,
      @PathVariable String itemName,
      @RequestParam Integer currentLevel)
      throws MarketplaceNotFoundException, ItemNotFoundException, AvatarCommunicationException {

    String avatarId = marketplaceService.getAvatarId(idOfMarketplace(marketplaceId)).value();
    log.info(
        currentLevel, "Buy request received from avatar " + avatarId + " for item " + itemName);

    if (!marketplaceService.canBuyItem(
        idOfMarketplace(marketplaceId), itemName, new Level(currentLevel))) {
      log.warn(
          idOfMarketplace(marketplaceId),
          "Avatar " + avatarId + " cannot buy item '" + itemName + "' due to insufficient level");
      return ResponseEntity.status(403).build();
    }

    Item item = marketplaceService.getAvailableItem(idOfMarketplace(marketplaceId), itemName);
    Money price = item.price();
    log.info(item, "Starting buy saga for avatar " + avatarId);

    boolean moneySpent = false;
    boolean inventoryAdded = false;
    try {
      avatarClient.spendMoney(avatarId, price);
      moneySpent = true;

      avatarClient.addItemToInventory(avatarId, item);
      inventoryAdded = true;

      marketplaceService.buyItem(idOfMarketplace(marketplaceId), itemName);
      log.info(item, "Buy saga completed for avatar " + avatarId);
      return ResponseEntity.noContent().build();

    } catch (RestClientException | AvatarCommunicationException ex) {
      log.error(item, "Buy saga failed for avatar " + avatarId, ex);
      try {
        if (inventoryAdded) {
          avatarClient.removeItemFromInventory(avatarId, item);
        }
        if (moneySpent) {
          avatarClient.earnMoney(avatarId, price);
        }
      } catch (RestClientException | AvatarCommunicationException compensationEx) {
        log.error(item, "Buy saga compensation failed for avatar " + avatarId, compensationEx);
        throw new AvatarCommunicationException(
            "Partial failure and compensation failed during buy saga", compensationEx);
      }
      throw new AvatarCommunicationException(
          "Avatar operation failed during buy saga, compensation performed", ex);
    }
  }

  @PostMapping("/{marketplaceId}/sold-items/{itemName}/sell")
  public ResponseEntity<Void> sellItem(
      @PathVariable String marketplaceId, @PathVariable String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException, AvatarCommunicationException {

    String avatarId = marketplaceService.getAvatarId(idOfMarketplace(marketplaceId)).value();
    Item item = marketplaceService.getSoldItem(idOfMarketplace(marketplaceId), itemName);
    Money price = item.price();

    log.info(item, "Starting sell saga for avatar " + avatarId);

    boolean removedFromInventory = false;
    boolean earnedMoney = false;
    try {
      avatarClient.removeItemFromInventory(avatarId, item);
      removedFromInventory = true;

      avatarClient.earnMoney(avatarId, price);
      earnedMoney = true;

      marketplaceService.sellItem(idOfMarketplace(marketplaceId), itemName);
      log.info(item, "Sell saga completed for avatar " + avatarId);
      return ResponseEntity.noContent().build();

    } catch (RestClientException | AvatarCommunicationException ex) {
      log.error(item, "Sell saga failed for avatar " + avatarId, ex);
      try {
        if (earnedMoney) {
          avatarClient.spendMoney(avatarId, price);
        }
        if (removedFromInventory) {
          avatarClient.addItemToInventory(avatarId, item);
        }
      } catch (RestClientException | AvatarCommunicationException compensationEx) {
        log.error(item, "Sell saga compensation failed for avatar " + avatarId, compensationEx);
        throw new AvatarCommunicationException(
            "Partial failure and compensation failed during sell saga", compensationEx);
      }
      throw new AvatarCommunicationException(
          "Avatar operation failed during sell saga, compensation performed", ex);
    }
  }

  // ─── Exception handling ──────────────────────────────────────────────────────

  @ExceptionHandler(MarketplaceNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleMarketplaceNotFound(MarketplaceNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({ItemNotFoundException.class, AvatarNotFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(AvatarCommunicationException.class)
  public ResponseEntity<ErrorResponse> handleAvatarCommunicationError(
      AvatarCommunicationException ex) {
    log.error(ex, "Avatar service communication error", ex);
    return ResponseEntity.status(502).body(new ErrorResponse(ex.getMessage()));
  }

  // ─── HATEOAS helpers ────────────────────────────────────────────────────────

  private Link selfMarketplaceLink(String marketplaceId) {
    return linkTo(methodOn(MarketplaceController.class).getMarketplace(marketplaceId))
        .withSelfRel();
  }

  private EntityModel<ItemResponse> availableItemModel(String marketplaceId, Item item) {
    ItemResponse dto = ItemMapper.toResponse(item);
    return EntityModel.of(
        dto,
        linkTo(methodOn(MarketplaceController.class).getAvailableItem(marketplaceId, item.name()))
            .withSelfRel(),
        selfMarketplaceLink(marketplaceId),
        linkTo(methodOn(MarketplaceController.class).getAvailableItems(marketplaceId, ItemType.ALL))
            .withRel("items"),
        linkTo(methodOn(MarketplaceController.class).buyItem(marketplaceId, item.name(), 0))
            .withRel("buy"));
  }

  private EntityModel<ItemResponse> soldItemModel(String marketplaceId, Item item) {
    ItemResponse dto = ItemMapper.toResponse(item);
    return EntityModel.of(
        dto,
        linkTo(methodOn(MarketplaceController.class).getSoldItem(marketplaceId, item.name()))
            .withSelfRel(),
        selfMarketplaceLink(marketplaceId),
        linkTo(methodOn(MarketplaceController.class).getSoldItems(marketplaceId))
            .withRel("sold-items"),
        linkTo(methodOn(MarketplaceController.class).sellItem(marketplaceId, item.name()))
            .withRel("sell"));
  }

  // ─── Request / Response records ─────────────────────────────────────────────

  public record ErrorResponse(String message) {}

  public record CreateMarketplaceRequest(String avatarId) {}
}
