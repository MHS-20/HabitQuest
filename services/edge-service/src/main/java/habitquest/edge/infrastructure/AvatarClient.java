package habitquest.edge.infrastructure;

import common.hexagonal.Adapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@Component
public class AvatarClient {

  private static final Logger LOG = LoggerFactory.getLogger(AvatarClient.class);

  private final RestClient restClient;

  public AvatarClient(@Value("${services.avatar.base-url}") String avatarBaseUrl) {
    this.restClient = RestClient.builder().baseUrl(avatarBaseUrl).build();
  }

  public void createAvatar(String userId, String name) {
    LOG.info("Creating avatar for userId={}, name={}", userId, name);
    try {
      restClient
          .post()
          .uri("/api/v1/avatars")
          .body(new CreateAvatarRequest(userId, name))
          .retrieve()
          .onStatus(
              status -> status != HttpStatus.CREATED,
              (req, res) -> {
                LOG.error(
                    "Avatar creation failed for userId={}: HTTP {}", userId, res.getStatusCode());
                throw new AvatarCreationException(
                    "Avatar service returned " + res.getStatusCode() + " for userId=" + userId);
              })
          .toBodilessEntity();
      LOG.info("Avatar created successfully for userId={}", userId);
    } catch (RestClientException e) {
      LOG.error("Could not reach avatar-service for userId={}: {}", userId, e.getMessage());
      throw new AvatarCreationException("Avatar service unreachable: " + e.getMessage(), e);
    }
  }

  private record CreateAvatarRequest(String id, String name) {}
}
