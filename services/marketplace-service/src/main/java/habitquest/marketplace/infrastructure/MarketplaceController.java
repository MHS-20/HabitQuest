package habitquest.marketplace.infrastructure;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import habitquest.marketplace.application.AvatarNotFoundExpection;
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

  /** Returns a marketplace by ID with navigational links to its sub-resources. */
  @GetMapping("/{marketplaceId}")
  public ResponseEntity<EntityModel<Marketplace>> getMarketplace(@PathVariable String marketplaceId)
      throws MarketplaceNotFoundException {

    Marketplace marketplace = marketplaceService.getMarketplace(marketplaceId);

    EntityModel<Marketplace> model =
        EntityModel.of(
            marketplace,
            selfMarketplaceLink(marketplaceId),
            linkTo(methodOn(MarketplaceController.class).getItems(marketplaceId)).withRel("items"),
            linkTo(methodOn(MarketplaceController.class).getArmors(marketplaceId))
                .withRel("armors"),
            linkTo(methodOn(MarketplaceController.class).getWeapons(marketplaceId))
                .withRel("weapons"),
            linkTo(methodOn(MarketplaceController.class).getPotions(marketplaceId))
                .withRel("potions"),
            linkTo(methodOn(MarketplaceController.class).getHealthPotions(marketplaceId))
                .withRel("healthPotions"),
            linkTo(methodOn(MarketplaceController.class).getManaPotions(marketplaceId))
                .withRel("manaPotions"));

    return ResponseEntity.ok(model);
  }

  // ─── Items ──────────────────────────────────────────────────────────────────

  /** Returns all items available in the marketplace. */
  @GetMapping("/{marketplaceId}/items")
  public ResponseEntity<CollectionModel<EntityModel<Item>>> getItems(
      @PathVariable String marketplaceId) throws MarketplaceNotFoundException {

    List<EntityModel<Item>> items =
        marketplaceService.getItems(marketplaceId).stream()
            .map(item -> itemModel(marketplaceId, item))
            .toList();

    CollectionModel<EntityModel<Item>> model =
        CollectionModel.of(
            items,
            linkTo(methodOn(MarketplaceController.class).getItems(marketplaceId)).withSelfRel(),
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

  /** Returns all armors in the marketplace. */
  @GetMapping("/{marketplaceId}/items/armors")
  public ResponseEntity<CollectionModel<EntityModel<Item>>> getArmors(
      @PathVariable String marketplaceId) throws MarketplaceNotFoundException {

    List<EntityModel<Item>> armors =
        marketplaceService.getArmors(marketplaceId).stream()
            .map(item -> itemModel(marketplaceId, item))
            .toList();

    CollectionModel<EntityModel<Item>> model =
        CollectionModel.of(
            armors,
            linkTo(methodOn(MarketplaceController.class).getArmors(marketplaceId)).withSelfRel(),
            selfMarketplaceLink(marketplaceId),
            linkTo(methodOn(MarketplaceController.class).getItems(marketplaceId)).withRel("items"));

    return ResponseEntity.ok(model);
  }

  /** Returns all weapons in the marketplace. */
  @GetMapping("/{marketplaceId}/items/weapons")
  public ResponseEntity<CollectionModel<EntityModel<Item>>> getWeapons(
      @PathVariable String marketplaceId) throws MarketplaceNotFoundException {

    List<EntityModel<Item>> weapons =
        marketplaceService.getWeapons(marketplaceId).stream()
            .map(item -> itemModel(marketplaceId, item))
            .toList();

    CollectionModel<EntityModel<Item>> model =
        CollectionModel.of(
            weapons,
            linkTo(methodOn(MarketplaceController.class).getWeapons(marketplaceId)).withSelfRel(),
            selfMarketplaceLink(marketplaceId),
            linkTo(methodOn(MarketplaceController.class).getItems(marketplaceId)).withRel("items"));

    return ResponseEntity.ok(model);
  }

  /** Returns all potions (health + mana) in the marketplace. */
  @GetMapping("/{marketplaceId}/items/potions")
  public ResponseEntity<CollectionModel<EntityModel<Item>>> getPotions(
      @PathVariable String marketplaceId) throws MarketplaceNotFoundException {

    List<EntityModel<Item>> potions =
        marketplaceService.getPotions(marketplaceId).stream()
            .map(item -> itemModel(marketplaceId, item))
            .toList();

    CollectionModel<EntityModel<Item>> model =
        CollectionModel.of(
            potions,
            linkTo(methodOn(MarketplaceController.class).getPotions(marketplaceId)).withSelfRel(),
            selfMarketplaceLink(marketplaceId),
            linkTo(methodOn(MarketplaceController.class).getHealthPotions(marketplaceId))
                .withRel("healthPotions"),
            linkTo(methodOn(MarketplaceController.class).getManaPotions(marketplaceId))
                .withRel("manaPotions"),
            linkTo(methodOn(MarketplaceController.class).getItems(marketplaceId)).withRel("items"));

    return ResponseEntity.ok(model);
  }

  /** Returns all health potions in the marketplace. */
  @GetMapping("/{marketplaceId}/items/potions/health")
  public ResponseEntity<CollectionModel<EntityModel<Item>>> getHealthPotions(
      @PathVariable String marketplaceId) throws MarketplaceNotFoundException {

    List<EntityModel<Item>> potions =
        marketplaceService.getHealthPotions(marketplaceId).stream()
            .map(item -> itemModel(marketplaceId, item))
            .toList();

    CollectionModel<EntityModel<Item>> model =
        CollectionModel.of(
            potions,
            linkTo(methodOn(MarketplaceController.class).getHealthPotions(marketplaceId))
                .withSelfRel(),
            selfMarketplaceLink(marketplaceId),
            linkTo(methodOn(MarketplaceController.class).getPotions(marketplaceId))
                .withRel("potions"));

    return ResponseEntity.ok(model);
  }

  /** Returns all mana potions in the marketplace. */
  @GetMapping("/{marketplaceId}/items/potions/mana")
  public ResponseEntity<CollectionModel<EntityModel<Item>>> getManaPotions(
      @PathVariable String marketplaceId) throws MarketplaceNotFoundException {

    List<EntityModel<Item>> potions =
        marketplaceService.getManaPotions(marketplaceId).stream()
            .map(item -> itemModel(marketplaceId, item))
            .toList();

    CollectionModel<EntityModel<Item>> model =
        CollectionModel.of(
            potions,
            linkTo(methodOn(MarketplaceController.class).getManaPotions(marketplaceId))
                .withSelfRel(),
            selfMarketplaceLink(marketplaceId),
            linkTo(methodOn(MarketplaceController.class).getPotions(marketplaceId))
                .withRel("potions"));

    return ResponseEntity.ok(model);
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

  @ExceptionHandler({ItemNotFoundException.class, AvatarNotFoundExpection.class})
  public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(AvatarCommunicationException.class)
  public ResponseEntity<ErrorResponse> handleAvatarCommunicationError(
      AvatarCommunicationException ex) {
    LOG.error("Avatar service communication error: {}", ex.getMessage());
    return ResponseEntity.status(502).body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  public ResponseEntity<ErrorResponse> handleDomainError(RuntimeException ex) {
    return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
  }

  // ─── HATEOAS helpers ────────────────────────────────────────────────────────

  private Link selfMarketplaceLink(String marketplaceId) {
    try {
      return linkTo(methodOn(MarketplaceController.class).getMarketplace(marketplaceId))
          .withSelfRel();
    } catch (MarketplaceNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private EntityModel<Item> itemModel(String marketplaceId, Item item) {
    try {
      return EntityModel.of(
          item,
          linkTo(methodOn(MarketplaceController.class).getItem(marketplaceId, item.name()))
              .withSelfRel(),
          selfMarketplaceLink(marketplaceId),
          linkTo(methodOn(MarketplaceController.class).getItems(marketplaceId)).withRel("items"),
          linkTo(methodOn(MarketplaceController.class).buyItem(marketplaceId, item.name(), null))
              .withRel("buy"),
          linkTo(methodOn(MarketplaceController.class).sellItem(marketplaceId, item.name(), null))
              .withRel("sell"));
    } catch (MarketplaceNotFoundException | ItemNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  // ─── Request / Response records ─────────────────────────────────────────────

  public record AvatarRequest(String avatarId) {}

  public record ErrorResponse(String message) {}
}
