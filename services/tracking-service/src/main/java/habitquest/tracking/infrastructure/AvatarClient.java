package habitquest.tracking.infrastructure;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.tracking.application.AvatarClientPort;
import habitquest.tracking.application.HabitLogger;
import habitquest.tracking.domain.Avatar;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@Component
public class AvatarClient implements AvatarClientPort {

  public static final String AVATAR_CLIENT = "avatarClient";

  private final RestClient restClient;
  private final HabitLogger log;

  public AvatarClient(RestClient avatarRestClient, HabitLogger log) {
    this.restClient = avatarRestClient;
    this.log = log;
  }

  @Override
  @CircuitBreaker(name = AVATAR_CLIENT, fallbackMethod = "grantExperienceFallback")
  @Retry(name = AVATAR_CLIENT)
  public void grantExperience(Id<Avatar> avatarId, int amount) {
    GrantExperienceRequest request = new GrantExperienceRequest(avatarId.value(), amount);
    log.info(request, "Granting experience to avatar from tracking-service");
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/experience/grant", avatarId.value())
          .body(new AmountRequest(amount))
          .retrieve()
          .toBodilessEntity();
      log.info(request, "Experience granted to avatar");
    } catch (RestClientException e) {
      log.error(request, "Failed to grant experience to avatar", e);
      throw new AvatarCommunicationException(
          "Failed to grant experience to avatar " + avatarId.value(), e);
    }
  }

  @Override
  @CircuitBreaker(name = AVATAR_CLIENT, fallbackMethod = "applyDamageFallback")
  @Retry(name = AVATAR_CLIENT)
  public void applyDamage(Id<Avatar> avatarId, int amount) {
    DamageRequest request = new DamageRequest(avatarId.value(), amount);
    log.info(request, "Applying damage to avatar from tracking-service");
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/health/damage", avatarId.value())
          .body(new AmountRequest(amount))
          .retrieve()
          .toBodilessEntity();
      log.info(request, "Damage applied to avatar");
    } catch (RestClientException e) {
      log.error(request, "Failed to apply damage to avatar", e);
      throw new AvatarCommunicationException(
          "Failed to apply damage to avatar " + avatarId.value(), e);
    }
  }

  private void grantExperienceFallback(Id<Avatar> avatarId, int amount, Exception ex) {
    GrantExperienceRequest request = new GrantExperienceRequest(avatarId.value(), amount);
    log.error(request, "Circuit breaker OPEN while granting experience", ex);
    throw new AvatarCommunicationException(
        "Avatar service non disponibile (experience) per " + avatarId.value(), ex);
  }

  private void applyDamageFallback(Id<Avatar> avatarId, int amount, Exception ex) {
    DamageRequest request = new DamageRequest(avatarId.value(), amount);
    log.error(request, "Circuit breaker OPEN while applying damage", ex);
    throw new AvatarCommunicationException(
        "Avatar service non disponibile (damage) per " + avatarId.value(), ex);
  }

  private record AmountRequest(Integer amount) {}

  private record GrantExperienceRequest(String avatarId, int amount) {}

  private record DamageRequest(String avatarId, int amount) {}
}
