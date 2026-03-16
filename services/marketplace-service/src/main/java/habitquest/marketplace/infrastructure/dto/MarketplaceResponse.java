package habitquest.marketplace.infrastructure.dto;

import habitquest.marketplace.domain.Marketplace;
import java.util.List;

public record MarketplaceResponse(String id, List<ItemResponse> items) {

  public static MarketplaceResponse from(Marketplace marketplace) {
    List<ItemResponse> items =
        marketplace.getCatalogItems().stream().map(ItemMapper::toResponse).toList();
    return new MarketplaceResponse(marketplace.getId(), items);
  }
}
