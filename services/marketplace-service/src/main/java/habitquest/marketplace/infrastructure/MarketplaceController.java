package habitquest.marketplace.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import common.ddd.Id;
import habitquest.marketplace.Avatar;
import habitquest.marketplace.application.AvatarNotFoundException;
import habitquest.marketplace.application.MarketplaceNotFoundException;
import habitquest.marketplace.application.MarketplaceService;
import habitquest.marketplace.domain.ItemNotFoundException;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.Money;
import habitquest.marketplace.domain.items.*;
import habitquest.marketplace.infrastructure.dto.ItemMapper;
import habitquest.marketplace.infrastructure.dto.ItemResponse;
import habitquest.marketplace.infrastructure.dto.MarketplaceResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger LOG = LoggerFactory.getLogger(MarketplaceController.class);

  private final MarketplaceService marketplaceService;
  private final AvatarClient avatarClient;

  public MarketplaceController(MarketplaceService marketplaceService, AvatarClient avatarClient) {
    this.marketplaceService = marketplaceService;
    this.avatarClient = avatarClient;
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

    MarketplaceResponse dto =
        MarketplaceResponse.from(marketplaceService.getMarketplace(idOfMarketplace(marketplaceId)));
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

  @PostMapping
  public ResponseEntity<EntityModel<MarketplaceResponse>> createMarketplace(
      @RequestBody CreateMarketplaceRequest request) throws AvatarCommunicationException {
    String avatarId = request.avatarId();
    LOG.info("Creating marketplace for avatar {}", avatarId);

    String marketplaceId =
        marketplaceService.createMarketplaceForAvatar(idOfAvatar(avatarId)).value();

    MarketplaceResponse dto =
        MarketplaceResponse.from(marketplaceService.getMarketplace(idOfMarketplace(marketplaceId)));
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

  /** Returns all available items, optionally filtered by type. */
  @GetMapping("/{marketplaceId}/items")
  public ResponseEntity<CollectionModel<EntityModel<ItemResponse>>> getAvailableItems(
      @PathVariable String marketplaceId, @RequestParam(defaultValue = "ALL") ItemType type)
      throws MarketplaceNotFoundException {

    List<Item> items =
        type == ItemType.ALL
            ? marketplaceService.getAllAvailableItems(idOfMarketplace(marketplaceId))
            : marketplaceService.getAvailableItemsByType(idOfMarketplace(marketplaceId), type);

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

  /** Returns a single available item by name. */
  @GetMapping("/{marketplaceId}/items/{itemName}")
  public ResponseEntity<EntityModel<ItemResponse>> getAvailableItem(
      @PathVariable String marketplaceId, @PathVariable String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException {
    Item item = marketplaceService.getAvailableItem(idOfMarketplace(marketplaceId), itemName);
    return ResponseEntity.ok(availableItemModel(marketplaceId, item));
  }

  // ─── Sold Items ─────────────────────────────────────────────────────────────

  /** Returns all sold items. */
  @GetMapping("/{marketplaceId}/sold-items")
  public ResponseEntity<CollectionModel<EntityModel<ItemResponse>>> getSoldItems(
      @PathVariable String marketplaceId) throws MarketplaceNotFoundException {

    List<EntityModel<ItemResponse>> itemModels =
        marketplaceService.getSoldItems(idOfMarketplace(marketplaceId)).stream()
            .map(item -> soldItemModel(marketplaceId, item))
            .toList();

    CollectionModel<EntityModel<ItemResponse>> model =
        CollectionModel.of(
            itemModels,
            linkTo(methodOn(MarketplaceController.class).getSoldItems(marketplaceId)).withSelfRel(),
            selfMarketplaceLink(marketplaceId));
    return ResponseEntity.ok(model);
  }

  /** Returns a single sold item by name. */
  @GetMapping("/{marketplaceId}/sold-items/{itemName}")
  public ResponseEntity<EntityModel<ItemResponse>> getSoldItem(
      @PathVariable String marketplaceId, @PathVariable String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException {
    Item item = marketplaceService.getSoldItem(idOfMarketplace(marketplaceId), itemName);
    return ResponseEntity.ok(soldItemModel(marketplaceId, item));
  }

  // ─── Commands ───────────────────────────────────────────────────────────────

  @PostMapping("/{marketplaceId}/items/{itemName}/buy")
  public ResponseEntity<Void> buyItem(
      @PathVariable String marketplaceId, @PathVariable String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException, AvatarCommunicationException {
    String avatarId = marketplaceService.getAvatarId(idOfMarketplace(marketplaceId)).value();
    LOG.info(
        "Starting buy saga: avatar {} buying item '{}' from marketplace {}",
        avatarId,
        itemName,
        marketplaceId);

    Item item = marketplaceService.getAvailableItem(idOfMarketplace(marketplaceId), itemName);
    Money price = item.price();

    boolean moneySpent = false;
    boolean inventoryAdded = false;
    try {
      avatarClient.spendMoney(avatarId, price);
      moneySpent = true;

      avatarClient.addItemToInventory(avatarId, item);
      inventoryAdded = true;

      marketplaceService.buyItem(idOfMarketplace(marketplaceId), itemName);
      return ResponseEntity.noContent().build();
    } catch (RestClientException | AvatarCommunicationException ex) {
      LOG.error("Buy saga error for avatar {} item {}: {}", avatarId, itemName, ex.getMessage());
      try {
        if (inventoryAdded) {
          avatarClient.removeItemFromInventory(avatarId, item);
        }
        if (moneySpent) {
          avatarClient.earnMoney(avatarId, price);
        }
      } catch (RestClientException | AvatarCommunicationException compensationEx) {
        LOG.error(
            "Buy saga compensation failed for avatar {} item {}: {}",
            avatarId,
            itemName,
            compensationEx.getMessage());
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
    LOG.info(
        "Starting sell saga: avatar {} selling item '{}' to marketplace {}",
        avatarId,
        itemName,
        marketplaceId);

    Item item = marketplaceService.getSoldItem(idOfMarketplace(marketplaceId), itemName);
    Money price = item.price();

    boolean removedFromInventory = false;
    boolean earnedMoney = false;
    try {
      avatarClient.removeItemFromInventory(avatarId, item);
      removedFromInventory = true;

      avatarClient.earnMoney(avatarId, price);
      earnedMoney = true;

      marketplaceService.sellItem(idOfMarketplace(marketplaceId), itemName);
      return ResponseEntity.noContent().build();
    } catch (RestClientException | AvatarCommunicationException ex) {
      LOG.error("Sell saga error for avatar {} item {}: {}", avatarId, itemName, ex.getMessage());
      try {
        if (earnedMoney) {
          avatarClient.spendMoney(avatarId, price);
        }
        if (removedFromInventory) {
          avatarClient.addItemToInventory(avatarId, item);
        }
      } catch (RestClientException | AvatarCommunicationException compensationEx) {
        LOG.error(
            "Sell saga compensation failed for avatar {} item {}: {}",
            avatarId,
            itemName,
            compensationEx.getMessage());
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
    LOG.error("Avatar service communication error: {}", ex.getMessage());
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
        linkTo(methodOn(MarketplaceController.class).buyItem(marketplaceId, item.name()))
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
