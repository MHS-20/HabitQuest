package habitquest.marketplace.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import common.ddd.Id;
import habitquest.marketplace.application.*;
import habitquest.marketplace.domain.Avatar;
import habitquest.marketplace.domain.ItemNotFoundException;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.items.*;
import habitquest.marketplace.infrastructure.dto.ItemResponse;
import habitquest.marketplace.infrastructure.dto.MarketplaceResponse;
import habitquest.marketplace.infrastructure.dto.MarketplaceResponseAssembler;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/marketplaces")
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class MarketplaceController {

  private final MarketplaceService marketplaceService;
  private final MarketplaceLogger log;
  private final MarketplaceResponseAssembler assembler;

  public MarketplaceController(
      MarketplaceService marketplaceService,
      MarketplaceLogger log,
      MarketplaceResponseAssembler assembler) {
    this.marketplaceService = marketplaceService;
    this.log = log;
    this.assembler = assembler;
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

    return ResponseEntity.ok(assembler.toModel(marketplace));
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

    return ResponseEntity.created(
            linkTo(methodOn(MarketplaceController.class).getMarketplace(marketplaceId)).toUri())
        .body(assembler.toModel(marketplace));
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
    return ResponseEntity.ok(assembler.toAvailableItemsCollection(marketplaceId, items, type));
  }

  @GetMapping("/{marketplaceId}/items/{itemName}")
  public ResponseEntity<EntityModel<ItemResponse>> getAvailableItem(
      @PathVariable String marketplaceId, @PathVariable String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException {

    Item item = marketplaceService.getAvailableItem(idOfMarketplace(marketplaceId), itemName);

    log.info(item, "Fetched available item");

    return ResponseEntity.ok(assembler.toAvailableItemModel(marketplaceId, item));
  }

  // ─── Sold Items ─────────────────────────────────────────────────────────────

  @GetMapping("/{marketplaceId}/sold-items")
  public ResponseEntity<CollectionModel<EntityModel<ItemResponse>>> getSoldItems(
      @PathVariable String marketplaceId) throws MarketplaceNotFoundException {
    List<Item> items = marketplaceService.getSoldItems(idOfMarketplace(marketplaceId));
    log.info(items, "Fetched sold items");
    return ResponseEntity.ok(assembler.toSoldItemsCollection(marketplaceId, items));
  }

  @GetMapping("/{marketplaceId}/sold-items/{itemName}")
  public ResponseEntity<EntityModel<ItemResponse>> getSoldItem(
      @PathVariable String marketplaceId, @PathVariable String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException {
    Item item = marketplaceService.getSoldItem(idOfMarketplace(marketplaceId), itemName);
    log.info(item, "Fetched sold item");
    return ResponseEntity.ok(assembler.toSoldItemModel(marketplaceId, item));
  }

  // ─── Commands ───────────────────────────────────────────────────────────────

  @PostMapping("/{marketplaceId}/items/{itemName}/buy")
  public ResponseEntity<Void> buyItem(
      @PathVariable String marketplaceId,
      @PathVariable String itemName,
      @RequestParam Integer currentLevel)
      throws MarketplaceNotFoundException, ItemNotFoundException, AvatarCommunicationException {
    marketplaceService.buyItem(idOfMarketplace(marketplaceId), itemName, new Level(currentLevel));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{marketplaceId}/sold-items/{itemName}/sell")
  public ResponseEntity<Void> sellItem(
      @PathVariable String marketplaceId, @PathVariable String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException, AvatarCommunicationException {
    marketplaceService.sellItem(idOfMarketplace(marketplaceId), itemName);
    return ResponseEntity.noContent().build();
  }

  // ─── Exception handling ─────────────────────────────────────────────────────

  @ExceptionHandler({
    ItemNotFoundException.class,
    AvatarNotFoundException.class,
    MarketplaceNotFoundException.class
  })
  public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler({InsufficientLevelException.class})
  public ResponseEntity<ErrorResponse> handleBadOperation(RuntimeException ex) {
    return ResponseEntity.status(403).body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(AvatarCommunicationException.class)
  public ResponseEntity<ErrorResponse> handleAvatarCommunicationError(
      AvatarCommunicationException ex) {

    log.error(ex, "Avatar service communication error", ex);

    return ResponseEntity.status(502).body(new ErrorResponse(ex.getMessage()));
  }

  // ─── Request / Response records ─────────────────────────────────────────────

  public record ErrorResponse(String message) {}

  public record CreateMarketplaceRequest(String avatarId) {}
}
