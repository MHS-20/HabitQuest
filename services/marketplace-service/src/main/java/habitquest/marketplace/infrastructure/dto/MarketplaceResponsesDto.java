package habitquest.marketplace.infrastructure.dto;

import habitquest.marketplace.domain.Marketplace;
import java.util.List;

public class MarketplaceResponsesDto {
  public record ErrorResponse(String message) {}

  public record ItemResponse(
      String type, String name, String description, Integer power, Integer price) {}

  public record MarketplaceResponse(String id, List<ItemResponse> items) {
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
