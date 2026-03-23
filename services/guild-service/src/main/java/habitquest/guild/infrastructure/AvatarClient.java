package habitquest.guild.infrastructure;

import common.hexagonal.Adapter;
import habitquest.guild.application.GuildLogger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@Component
public class AvatarClient {

  private final RestClient restClient;
  private final GuildLogger log;

  public AvatarClient(RestClient avatarRestClient, GuildLogger log) {
    this.restClient = avatarRestClient;
    this.log = log;
  }

  public DamageResult applyDamage(String avatarId, int amount) {
    AvatarRequest request = new AvatarRequest(avatarId, amount);
    log.info(request, "Applying damage to avatar");
    try {
      ResponseEntity<DamageResult> response =
          restClient
              .post()
              .uri("/api/v1/avatars/{id}/health/damage", avatarId)
              .body(new AmountRequest(amount))
              .retrieve()
              .toEntity(DamageResult.class);
      DamageResult result =
          response.getBody() != null ? response.getBody() : new DamageResult(false);
      log.info(result, "Damage applied to avatar: " + avatarId);
      return result;
    } catch (RestClientException e) {
      log.error(request, "Failed to apply damage to avatar: " + avatarId, e);
      throw new AvatarCommunicationException("Failed to apply damage to avatar " + avatarId, e);
    }
  }

  public void grantExperience(String avatarId, int amount) {
    AvatarRequest request = new AvatarRequest(avatarId, amount);
    log.info(request, "Granting experience to avatar");
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/experience/grant", avatarId)
          .body(new AmountRequest(amount))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      log.error(request, "Failed to grant experience to avatar: " + avatarId, e);
      throw new AvatarCommunicationException("Failed to grant experience to avatar " + avatarId, e);
    }
  }

  public void earnMoney(String avatarId, int amount) {
    AvatarRequest request = new AvatarRequest(avatarId, amount);
    log.info(request, "Granting money to avatar");
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/money/earn", avatarId)
          .body(new AmountRequest(amount))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      log.error(request, "Failed to grant money to avatar: " + avatarId, e);
      throw new AvatarCommunicationException("Failed to grant money to avatar " + avatarId, e);
    }
  }

  public void applyPenalty(String avatarId, int penaltyAmount) {
    log.info(new AvatarRequest(avatarId, penaltyAmount), "Applying penalty to avatar");
    applyDamage(avatarId, penaltyAmount);
  }

  private record AmountRequest(Integer amount) {}

  private record AvatarRequest(String avatarId, int amount) {}

  public record DamageResult(boolean died) {}
}
