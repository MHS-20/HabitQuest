package habitquest.guild.infrastructure;

import common.hexagonal.Adapter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@Component
public class AvatarClient {

  private final RestClient restClient;

  public AvatarClient(RestClient avatarRestClient) {
    this.restClient = avatarRestClient;
  }

  // ─── Combat ─────────────────────────────────────────────────────────────────

  public DamageResult applyDamage(String avatarId, int amount) {
    try {
      ResponseEntity<DamageResult> response =
          restClient
              .post()
              .uri("/api/v1/avatars/{id}/health/damage", avatarId)
              .body(new AmountRequest(amount))
              .retrieve()
              .toEntity(DamageResult.class);
      return response.getBody() != null ? response.getBody() : new DamageResult(false);
    } catch (RestClientException e) {
      throw new AvatarCommunicationException("Failed to apply damage to avatar " + avatarId, e);
    }
  }

  // ─── Rewards ────────────────────────────────────────────────────────────────

  public void grantExperience(String avatarId, int amount) {
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/experience/grant", avatarId)
          .body(new AmountRequest(amount))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      throw new AvatarCommunicationException("Failed to grant experience to avatar " + avatarId, e);
    }
  }

  public void earnMoney(String avatarId, int amount) {
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/money/earn", avatarId)
          .body(new AmountRequest(amount))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      throw new AvatarCommunicationException("Failed to grant money to avatar " + avatarId, e);
    }
  }

  // ─── Penalty ────────────────────────────────────────────────────────────────

  public void applyPenalty(String avatarId, int penaltyAmount) {
    applyDamage(avatarId, penaltyAmount);
  }

  // ─── Internal DTO ───────────────────────────────────────────────────────────

  private record AmountRequest(Integer amount) {}

  public record DamageResult(boolean died) {}
}
