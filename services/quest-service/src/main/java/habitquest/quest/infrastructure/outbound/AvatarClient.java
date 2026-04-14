package habitquest.quest.infrastructure.outbound;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.quest.application.exceptions.AvatarRewardException;
import habitquest.quest.application.port.out.AvatarClientPort;
import habitquest.quest.application.port.out.QuestLogger;
import habitquest.quest.domain.Avatar;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@Component
public class AvatarClient implements AvatarClientPort {

  private final RestClient restClient;
  private final QuestLogger log;

  public AvatarClient(RestClient avatarRestClient, QuestLogger log) {
    this.restClient = avatarRestClient;
    this.log = log;
  }

  @Override
  public void earnMoney(Id<Avatar> avatarId, int amount) {
    AmountRequest request = new AmountRequest(amount);
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/money/earn", avatarId.value())
          .body(request)
          .retrieve()
          .toBodilessEntity();
      log.info(request, "Granted quest completion money to avatar " + avatarId.value());
    } catch (RestClientException ex) {
      log.error(
          request, "Failed to grant quest completion money to avatar " + avatarId.value(), ex);
      throw new AvatarRewardException(
          "Failed to grant quest completion money to avatar " + avatarId.value(), ex);
    }
  }

  @Override
  public void applyDamage(Id<Avatar> avatarId, int amount) {
    AmountRequest request = new AmountRequest(amount);
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/health/damage", avatarId.value())
          .body(request)
          .retrieve()
          .toBodilessEntity();
      log.info(request, "Applied quest penalty damage to avatar " + avatarId.value());
    } catch (RestClientException ex) {
      log.error(request, "Failed to apply quest penalty damage to avatar " + avatarId.value(), ex);
      throw new AvatarRewardException(
          "Failed to apply quest penalty damage to avatar " + avatarId.value(), ex);
    }
  }

  private record AmountRequest(Integer amount) {}
}
