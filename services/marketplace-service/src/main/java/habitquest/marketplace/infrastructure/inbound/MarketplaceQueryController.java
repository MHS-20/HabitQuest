package habitquest.marketplace.infrastructure.inbound;

import common.ddd.Id;
import habitquest.marketplace.application.exceptions.MarketplaceNotFoundException;
import habitquest.marketplace.application.port.in.MarketplaceQueryService;
import habitquest.marketplace.application.port.out.MarketplaceLogger;
import habitquest.marketplace.domain.exceptions.ItemNotFoundException;
import habitquest.marketplace.domain.items.*;
import habitquest.marketplace.domain.marketplace.Avatar;
import habitquest.marketplace.domain.marketplace.Marketplace;
import habitquest.marketplace.infrastructure.dto.MarketplaceQueries.*;
import habitquest.marketplace.infrastructure.dto.MarketplaceResponseAssembler;
import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/marketplaces")
public class MarketplaceQueryController {

  private final MarketplaceQueryService queryService;
  private final MarketplaceLogger log;
  private final MarketplaceResponseAssembler assembler;

  public MarketplaceQueryController(
      MarketplaceQueryService queryService,
      MarketplaceLogger log,
      MarketplaceResponseAssembler assembler) {
    this.queryService = queryService;
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

    Marketplace marketplace = queryService.getMarketplace(idOfMarketplace(marketplaceId));

    log.info(marketplace, "Fetched marketplace");

    return ResponseEntity.ok(assembler.toModel(marketplace));
  }

  @GetMapping("/by-avatar/{avatarId}")
  public ResponseEntity<EntityModel<MarketplaceResponse>> getMarketplaceByAvatarId(
      @PathVariable String avatarId) {

    Id<Marketplace> marketplaceId = queryService.getMarketplaceIdByAvatarId(idOfAvatar(avatarId));

    return getMarketplace(marketplaceId.value());
  }

  // ─── Available Items ────────────────────────────────────────────────────────

  @GetMapping("/{marketplaceId}/items")
  public ResponseEntity<CollectionModel<EntityModel<ItemResponse>>> getAvailableItems(
      @PathVariable String marketplaceId, @RequestParam(defaultValue = "ALL") ItemFilter type)
      throws MarketplaceNotFoundException {

    List<Item> items =
        type == ItemFilter.ALL
            ? queryService.getAllAvailableItems(idOfMarketplace(marketplaceId))
            : queryService.getAvailableItemsByType(idOfMarketplace(marketplaceId), type);

    log.info(items, "Fetched available items");

    return ResponseEntity.ok(assembler.toAvailableItemsCollection(marketplaceId, items, type));
  }

  @GetMapping("/{marketplaceId}/items/{itemName}")
  public ResponseEntity<EntityModel<ItemResponse>> getAvailableItem(
      @PathVariable String marketplaceId, @PathVariable String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException {

    Item item = queryService.getAvailableItem(idOfMarketplace(marketplaceId), itemName);

    log.info(item, "Fetched available item");

    return ResponseEntity.ok(assembler.toAvailableItemModel(marketplaceId, item));
  }

  // ─── Sold Items ─────────────────────────────────────────────────────────────

  @GetMapping("/{marketplaceId}/sold-items")
  public ResponseEntity<CollectionModel<EntityModel<ItemResponse>>> getSoldItems(
      @PathVariable String marketplaceId) throws MarketplaceNotFoundException {

    List<Item> items = queryService.getSoldItems(idOfMarketplace(marketplaceId));

    log.info(items, "Fetched sold items");

    return ResponseEntity.ok(assembler.toSoldItemsCollection(marketplaceId, items));
  }

  @GetMapping("/{marketplaceId}/sold-items/{itemName}")
  public ResponseEntity<EntityModel<ItemResponse>> getSoldItem(
      @PathVariable String marketplaceId, @PathVariable String itemName)
      throws MarketplaceNotFoundException, ItemNotFoundException {

    Item item = queryService.getSoldItem(idOfMarketplace(marketplaceId), itemName);

    log.info(item, "Fetched sold item");

    return ResponseEntity.ok(assembler.toSoldItemModel(marketplaceId, item));
  }
}
