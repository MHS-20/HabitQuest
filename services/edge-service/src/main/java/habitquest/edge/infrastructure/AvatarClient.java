package habitquest.edge.infrastructure;

import common.hexagonal.Adapter;
import habitquest.edge.application.EdgeLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@Component
public class AvatarClient {

  private final RestClient restClient;
  private final EdgeLogger log;

  public AvatarClient(@Value("${services.avatar.base-url}") String avatarBaseUrl, EdgeLogger log) {
    this.restClient = RestClient.builder().baseUrl(avatarBaseUrl).build();
    this.log = log;
  }

  public void createAvatar(String userId, String name) {
    CreateAvatarRequest request = new CreateAvatarRequest(userId, name);
    log.info(request, "Creating avatar");
    try {
      restClient
          .post()
          .uri("/api/v1/avatars")
          .body(request)
          .retrieve()
          .onStatus(
              status -> status != HttpStatus.CREATED,
              (req, res) -> {
                log.error(request, "Avatar creation failed: HTTP " + res.getStatusCode(), null);
                throw new AvatarCreationException(
                    "Avatar service returned " + res.getStatusCode() + " for userId=" + userId);
              })
          .toBodilessEntity();
      log.info(request, "Avatar created successfully");
    } catch (RestClientException e) {
      log.error(request, "Could not reach avatar-service", e);
      throw new AvatarCreationException("Avatar service unreachable: " + e.getMessage(), e);
    }
  }

  private record CreateAvatarRequest(String id, String name) {}
}
