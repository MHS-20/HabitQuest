package habitquest.marketplace.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import habitquest.marketplace.application.AvatarNotFoundException;
import habitquest.marketplace.application.ItemNotFoundException;
import habitquest.marketplace.application.MarketplaceNotFoundException;
import habitquest.marketplace.application.MarketplaceService;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.Money;
import habitquest.marketplace.domain.items.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

  // ─── Marketplace ────────────────────────────────────────────────────────────
  @GetMapping("/{marketplaceId}")
  public ResponseEntity<EntityModel<Marketplace>> getMarketplace(
      @PathVariable String marketplaceId) {
    Marketplace marketplace = marketplaceService.getMarketplace(marketplaceId);
    EntityModel<Marketplace> model =
        EntityModel.of(
            marketplace,
            selfMarketplaceLink(marketplaceId),
            linkTo(methodOn(MarketplaceController.class).getItems(marketplaceId, ItemType.ALL))
                .withRel("items"));
    return ResponseEntity.ok(model);
  }

  // ─── Items ──────────────────────────────────────────────────────────────────

  /** Returns all items available in the marketplace. */
  @GetMapping("/{marketplaceId}/items")
  public ResponseEntity<CollectionModel<EntityModel<Item>>> getItems(
      @PathVariable String marketplaceId, @RequestParam(defaultValue = "ALL") ItemType type)
      throws MarketplaceNotFoundException {
    List<EntityModel<Item>> items =
        marketplaceService.getItems(marketplaceId, type).stream()
            .map(item -> itemModel(marketplaceId, item))
            .toList();
    CollectionModel<EntityModel<Item>> model =
        CollectionModel.of(
            items,
            linkTo(methodOn(MarketplaceController.class).getItems(marketplaceId, type))
                .withSelfRel(),
            selfMarketplaceLink(marketplaceId));
    return ResponseEntity.ok(model);
  }

  /** Returns a single item by name. */
  @GetMapping("/{marketplaceId}/items/{itemName}")
  public ResponseEntity<EntityModel<Item>> getItem(
      @PathVariable String marketplaceId, @PathVariable String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException {

    Item item = marketplaceService.getItemByName(marketplaceId, itemName);
    return ResponseEntity.ok(itemModel(marketplaceId, item));
  }

  // ─── Commands ───────────────────────────────────────────────────────────────
  @PostMapping("/{marketplaceId}/items/{itemName}/buy")
  public ResponseEntity<Void> buyItem(
      @PathVariable String marketplaceId,
      @PathVariable String itemName,
      @RequestBody AvatarRequest request)
      throws MarketplaceNotFoundException, ItemNotFoundException {

    LOG.info(
        "Avatar {} buying item '{}' from marketplace {}",
        request.avatarId(),
        itemName,
        marketplaceId);

    Item item = marketplaceService.getItemByName(marketplaceId, itemName);
    Money price = item.price();

    avatarClient.spendMoney(request.avatarId(), price);
    avatarClient.addItemToInventory(request.avatarId(), item);

    marketplaceService.buyItem(marketplaceId, itemName, request.avatarId());

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{marketplaceId}/items/{itemName}/sell")
  public ResponseEntity<Void> sellItem(
      @PathVariable String marketplaceId,
      @PathVariable String itemName,
      @RequestBody AvatarRequest request)
      throws MarketplaceNotFoundException, ItemNotFoundException {

    LOG.info(
        "Avatar {} selling item '{}' to marketplace {}",
        request.avatarId(),
        itemName,
        marketplaceId);

    Item item = marketplaceService.getItemByName(marketplaceId, itemName);
    Money price = item.price();

    avatarClient.removeItemFromInventory(request.avatarId(), item);
    avatarClient.earnMoney(request.avatarId(), price);

    marketplaceService.sellItem(marketplaceId, itemName, request.avatarId());

    return ResponseEntity.noContent().build();
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

  private EntityModel<Item> itemModel(String marketplaceId, Item item) {
    return EntityModel.of(
        item,
        linkTo(methodOn(MarketplaceController.class).getItem(marketplaceId, item.name()))
            .withSelfRel(),
        selfMarketplaceLink(marketplaceId),
        linkTo(methodOn(MarketplaceController.class).getItems(marketplaceId, ItemType.ALL))
            .withRel("items"),
        linkTo(methodOn(MarketplaceController.class).buyItem(marketplaceId, item.name(), null))
            .withRel("buy"),
        linkTo(methodOn(MarketplaceController.class).sellItem(marketplaceId, item.name(), null))
            .withRel("sell"));
  }

  // ─── Request / Response records ─────────────────────────────────────────────
  public record AvatarRequest(String avatarId) {}

  public record ErrorResponse(String message) {}
}
