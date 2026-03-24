package habitquest.edge.infrastructure;

import common.hexagonal.Adapter;
import habitquest.edge.application.EdgeLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Adapter
@Component
public class AvatarClient {

  private final WebClient webClient;
  private final EdgeLogger log;

  public AvatarClient(@Value("${services.avatar.base-url}") String avatarBaseUrl, EdgeLogger log) {
    this.webClient = WebClient.builder().baseUrl(avatarBaseUrl).build();
    this.log = log;
  }

  public Mono<Void> createAvatar(String userId, String name) {
    CreateAvatarRequest request = new CreateAvatarRequest(userId, name);
    log.info(request, "Creating avatar");
    return webClient
        .post()
        .uri("/api/v1/avatars")
        .bodyValue(request)
        .retrieve()
        .onStatus(
            status -> status != HttpStatus.CREATED,
            response ->
                response
                    .createException()
                    .flatMap(
                        ex -> {
                          log.error(
                              request, "Avatar creation failed: HTTP " + response.statusCode(), ex);
                          return Mono.error(
                              new AvatarCreationException(
                                  "Avatar service returned "
                                      + response.statusCode()
                                      + " for userId="
                                      + userId,
                                  ex));
                        }))
        .toBodilessEntity()
        .doOnSuccess(ignored -> log.info(request, "Avatar created successfully"))
        .onErrorMap(
            error -> {
              if (error instanceof AvatarCreationException) {
                return error;
              }
              log.error(request, "Could not reach avatar-service", error);
              return new AvatarCreationException(
                  "Avatar service unreachable: " + error.getMessage(), error);
            })
        .then();
  }

  private record CreateAvatarRequest(String id, String name) {}
}
