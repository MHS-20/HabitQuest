package habitquest.marketplace.infrastructure.dto;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import habitquest.marketplace.domain.items.Item;
import habitquest.marketplace.domain.items.ItemFilter;
import habitquest.marketplace.domain.marketplace.Marketplace;
import habitquest.marketplace.infrastructure.dto.MarketplaceQueries.*;
import habitquest.marketplace.infrastructure.inbound.MarketplaceCommandController;
import habitquest.marketplace.infrastructure.inbound.MarketplaceQueryController;
import java.util.List;
import org.springframework.hateoas.*;
import org.springframework.stereotype.Component;

@Component
public class MarketplaceResponseAssembler {

  // ─── Marketplace ────────────────────────────────────────────────────────────
  public EntityModel<MarketplaceResponse> toModel(Marketplace marketplace) {
    String marketplaceId = marketplace.getId().value();

    MarketplaceResponse dto = MarketplaceResponse.from(marketplace);

    return EntityModel.of(
        dto,
        selfMarketplaceLink(marketplaceId),
        linkTo(
                methodOn(MarketplaceQueryController.class)
                    .getAvailableItems(marketplaceId, ItemFilter.ALL))
            .withRel("items"),
        linkTo(methodOn(MarketplaceQueryController.class).getSoldItems(marketplaceId))
            .withRel("sold-items"));
  }

  // ─── Available Items ────────────────────────────────────────────────────────
  public EntityModel<ItemResponse> toAvailableItemModel(String marketplaceId, Item item) {

    ItemResponse dto = ItemMapper.toResponse(item);

    return EntityModel.of(
        dto,
        linkTo(
                methodOn(MarketplaceQueryController.class)
                    .getAvailableItem(marketplaceId, item.name()))
            .withSelfRel(),
        selfMarketplaceLink(marketplaceId),
        linkTo(
                methodOn(MarketplaceQueryController.class)
                    .getAvailableItems(marketplaceId, ItemFilter.ALL))
            .withRel("items"),
        linkTo(methodOn(MarketplaceCommandController.class).buyItem(marketplaceId, item.name(), 0))
            .withRel("buy"));
  }

  public CollectionModel<EntityModel<ItemResponse>> toAvailableItemsCollection(
      String marketplaceId, List<Item> items, ItemFilter type) {

    List<EntityModel<ItemResponse>> itemModels =
        items.stream().map(item -> toAvailableItemModel(marketplaceId, item)).toList();

    return CollectionModel.of(
        itemModels,
        linkTo(methodOn(MarketplaceQueryController.class).getAvailableItems(marketplaceId, type))
            .withSelfRel(),
        selfMarketplaceLink(marketplaceId));
  }

  // ─── Sold Items ─────────────────────────────────────────────────────────────
  public EntityModel<ItemResponse> toSoldItemModel(String marketplaceId, Item item) {

    ItemResponse dto = ItemMapper.toResponse(item);

    return EntityModel.of(
        dto,
        linkTo(methodOn(MarketplaceQueryController.class).getSoldItem(marketplaceId, item.name()))
            .withSelfRel(),
        selfMarketplaceLink(marketplaceId),
        linkTo(methodOn(MarketplaceQueryController.class).getSoldItems(marketplaceId))
            .withRel("sold-items"),
        linkTo(methodOn(MarketplaceCommandController.class).sellItem(marketplaceId, item.name()))
            .withRel("sell"));
  }

  public CollectionModel<EntityModel<ItemResponse>> toSoldItemsCollection(
      String marketplaceId, List<Item> items) {

    List<EntityModel<ItemResponse>> itemModels =
        items.stream().map(item -> toSoldItemModel(marketplaceId, item)).toList();

    return CollectionModel.of(
        itemModels,
        linkTo(methodOn(MarketplaceQueryController.class).getSoldItems(marketplaceId))
            .withSelfRel(),
        selfMarketplaceLink(marketplaceId));
  }

  // ─── Helpers ────────────────────────────────────────────────────────────────
  private Link selfMarketplaceLink(String marketplaceId) {
    return linkTo(methodOn(MarketplaceQueryController.class).getMarketplace(marketplaceId))
        .withSelfRel();
  }
}
