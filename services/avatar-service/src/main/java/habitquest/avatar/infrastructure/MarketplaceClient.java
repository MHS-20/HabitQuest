package habitquest.avatar.infrastructure;

import common.hexagonal.Adapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@Component
public class MarketplaceClient {

  private static final Logger LOG = LoggerFactory.getLogger(MarketplaceClient.class);

  private final RestClient restClient;

  public MarketplaceClient(RestClient marketplaceRestClient) {
    this.restClient = marketplaceRestClient;
  }

  public void createMarketplace(String avatarId) {
    LOG.info("Creating marketplace for avatar {}", avatarId);

    try {
      restClient
          .post()
          .uri("/api/v1/marketplaces")
          .body(new CreateMarketplaceRequest(avatarId))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException ex) {
      LOG.error("Failed to create marketplace for avatar {}: {}", avatarId, ex.getMessage());
      throw new MarketplaceCommunicationException(
          "Could not create marketplace for avatar " + avatarId, ex);
    }
  }

  record CreateMarketplaceRequest(String avatarId) {}
}
