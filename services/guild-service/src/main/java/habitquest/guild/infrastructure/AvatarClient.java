package habitquest.guild.infrastructure;

import common.hexagonal.Adapter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Adapter
@Component
public class AvatarClient {

  private final RestClient restClient;

  public AvatarClient(RestClient avatarRestClient) {
    this.restClient = avatarRestClient;
  }

  // ─── Combat ─────────────────────────────────────────────────────────────────

  public void applyDamage(String avatarId, int amount) {
    restClient
        .post()
        .uri("/api/v1/avatars/{id}/health/damage", avatarId)
        .body(new AmountRequest(amount))
        .retrieve()
        .toBodilessEntity();
  }

  // ─── Rewards ────────────────────────────────────────────────────────────────

  public void grantExperience(String avatarId, int amount) {
    restClient
        .post()
        .uri("/api/v1/avatars/{id}/experience/grant", avatarId)
        .body(new AmountRequest(amount))
        .retrieve()
        .toBodilessEntity();
  }

  public void earnMoney(String avatarId, int amount) {
    restClient
        .post()
        .uri("/api/v1/avatars/{id}/money/earn", avatarId)
        .body(new AmountRequest(amount))
        .retrieve()
        .toBodilessEntity();
  }

  // ─── Penalty ────────────────────────────────────────────────────────────────

  /**
   * Una penalty si traduce in danno diretto sull'avatar. Il campo Penalty.amount() rappresenta HP
   * sottratti.
   */
  public void applyPenalty(String avatarId, int penaltyAmount) {
    applyDamage(avatarId, penaltyAmount);
  }

  // ─── Internal DTO ───────────────────────────────────────────────────────────

  private record AmountRequest(Integer amount) {}
}
