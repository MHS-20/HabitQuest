package habitquest.avatar.infrastructure;

import common.hexagonal.Adapter;
import habitquest.avatar.application.AvatarLogger;
import habitquest.avatar.application.MarketplaceClientPort;
import habitquest.avatar.application.MarketplaceCommunicationException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@Component
public class MarketplaceClient implements MarketplaceClientPort {

  private final RestClient restClient;
  private final AvatarLogger log;

  public MarketplaceClient(RestClient marketplaceRestClient, AvatarLogger log) {
    this.restClient = marketplaceRestClient;
    this.log = log;
  }

  @CircuitBreaker(name = "marketplaceClient", fallbackMethod = "createMarketplaceFallback")
  @Retry(name = "marketplaceClient")
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

  private void createMarketplaceFallback(String avatarId, Exception ex) {
    log.error(
        new CreateMarketplaceRequest(avatarId),
        "Circuit breaker OPEN o retry esauriti per marketplace creation. avatarId=" + avatarId,
        ex);
    throw new MarketplaceCommunicationException(
        "Marketplace service non disponibile per avatar " + avatarId, ex);
  }

  record CreateMarketplaceRequest(String avatarId) {}
}
