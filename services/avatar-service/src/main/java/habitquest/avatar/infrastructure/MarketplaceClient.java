package habitquest.avatar.infrastructure;

import common.hexagonal.Adapter;
import habitquest.avatar.application.AvatarLogger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@Component
public class MarketplaceClient {

  private final RestClient restClient;
  private final AvatarLogger log;

  public MarketplaceClient(RestClient marketplaceRestClient, AvatarLogger log) {
    this.restClient = marketplaceRestClient;
    this.log = log;
  }

  public void createMarketplace(String avatarId) {
    CreateMarketplaceRequest request = new CreateMarketplaceRequest(avatarId);
    log.info(request, "Creating marketplace");

    try {
      restClient.post().uri("/api/v1/marketplaces").body(request).retrieve().toBodilessEntity();
    } catch (RestClientException ex) {
      log.error(request, "Failed to create marketplace", ex);
      throw new MarketplaceCommunicationException(
          "Could not create marketplace for avatar " + avatarId, ex);
    }
  }

  record CreateMarketplaceRequest(String avatarId) {}
}
