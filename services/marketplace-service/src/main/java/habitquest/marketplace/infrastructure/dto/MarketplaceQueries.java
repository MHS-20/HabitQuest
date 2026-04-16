package habitquest.marketplace.infrastructure.dto;

import common.cqrs.QueryResponse;
import habitquest.marketplace.domain.marketplace.Marketplace;
import java.util.List;

public class MarketplaceQueries {
  public record ErrorResponse(String message) implements QueryResponse {}

  public record ItemResponse(
      String type, String name, String description, Integer power, Integer price)
      implements QueryResponse {}

  public record MarketplaceResponse(String id, List<ItemResponse> items) implements QueryResponse {
    public MarketplaceResponse {
      items = List.copyOf(items);
    }

    public static MarketplaceResponse from(Marketplace marketplace) {
      List<ItemResponse> items =
          marketplace.getCatalogItems().stream().map(ItemMapper::toResponse).toList();
      return new MarketplaceResponse(marketplace.getId().value(), items);
    }
  }
}
