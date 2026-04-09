package habitquest.guild.infrastructure;

import common.hexagonal.Adapter;
import habitquest.guild.application.AvatarClientPort;
import habitquest.guild.application.AvatarCommunicationException;
import habitquest.guild.application.GuildLogger;
import habitquest.guild.domain.battle.DamageResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@Component
public class AvatarClient implements AvatarClientPort {

  public static final String AVATAR_CLIENT = "avatarClient";
  private final RestClient restClient;
  private final GuildLogger log;

  public AvatarClient(RestClient avatarRestClient, GuildLogger log) {
    this.restClient = avatarRestClient;
    this.log = log;
  }

  @CircuitBreaker(name = AVATAR_CLIENT, fallbackMethod = "applyDamageFallback")
  @Retry(name = AVATAR_CLIENT)
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

  @CircuitBreaker(name = AVATAR_CLIENT, fallbackMethod = "applyPenaltyFallback")
  @Retry(name = AVATAR_CLIENT)
  public void applyPenalty(String avatarId, int penaltyAmount) {
    log.info(new AvatarRequest(avatarId, penaltyAmount), "Applying penalty to avatar");
    AvatarRequest request = new AvatarRequest(avatarId, penaltyAmount);
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/health/damage", avatarId)
          .body(new AmountRequest(penaltyAmount))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      log.error(request, "Failed to apply penalty to avatar: " + avatarId, e);
      throw new AvatarCommunicationException("Failed to apply penalty to avatar " + avatarId, e);
    }
  }

  @CircuitBreaker(name = AVATAR_CLIENT, fallbackMethod = "grantExperienceFallback")
  @Retry(name = AVATAR_CLIENT)
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

  @CircuitBreaker(name = AVATAR_CLIENT, fallbackMethod = "earnMoneyFallback")
  @Retry(name = AVATAR_CLIENT)
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

  @CircuitBreaker(name = AVATAR_CLIENT, fallbackMethod = "sendInviteToAvatarFallback")
  @Retry(name = AVATAR_CLIENT)
  public void sendInviteToAvatar(
      String inviteId, String avatarId, String guildId, String guildName, Instant expiresAt) {
    AvatarRequest request = new AvatarRequest(avatarId, 0);
    log.info(request, "Sending guild invite to avatar");
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/invites", avatarId)
          .body(new GuildInviteRequest(inviteId, guildId, guildName, expiresAt.toString()))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException e) {
      log.error(request, "Failed to send guild invite to avatar: " + avatarId, e);
      throw new AvatarCommunicationException(
          "Failed to send guild invite to avatar " + avatarId, e);
    }
  }

  // ── Fallback methods ────────────────────────────────────────────────────────
  private DamageResult applyDamageFallback(String avatarId, int amount, Exception ex) {
    log.error(
        new AvatarRequest(avatarId, amount),
        "Circuit breaker OPEN per applyDamage, avatarId=" + avatarId,
        ex);
    throw new AvatarCommunicationException(
        "Avatar service non disponibile (damage) per " + avatarId, ex);
  }

  private void applyPenaltyFallback(String avatarId, int penaltyAmount, Exception ex) {
    log.error(
        new AvatarRequest(avatarId, penaltyAmount),
        "Circuit breaker OPEN per applyPenalty, avatarId=" + avatarId,
        ex);
    throw new AvatarCommunicationException(
        "Avatar service non disponibile (penalty) per " + avatarId, ex);
  }

  private void grantExperienceFallback(String avatarId, int amount, Exception ex) {
    log.error(
        new AvatarRequest(avatarId, amount),
        "Circuit breaker OPEN per grantExperience, avatarId=" + avatarId,
        ex);
    throw new AvatarCommunicationException(
        "Avatar service non disponibile (experience) per " + avatarId, ex);
  }

  private void earnMoneyFallback(String avatarId, int amount, Exception ex) {
    log.error(
        new AvatarRequest(avatarId, amount),
        "Circuit breaker OPEN per earnMoney, avatarId=" + avatarId,
        ex);
    throw new AvatarCommunicationException(
        "Avatar service non disponibile (money) per " + avatarId, ex);
  }

  private void sendInviteToAvatarFallback(
      String inviteId,
      String avatarId,
      String guildId,
      String guildName,
      Instant expiresAt,
      Throwable ex) {
    log.error(
        new AvatarRequest(avatarId, 0),
        "Circuit breaker OPEN per sendInviteToAvatar, avatarId=" + avatarId,
        ex);
    throw new AvatarCommunicationException(
        "Avatar service non disponibile (guild invite) per " + avatarId, ex);
  }

  private record AmountRequest(Integer amount) {}

  private record AvatarRequest(String avatarId, int amount) {}

  private record GuildInviteRequest(
      String inviteId, String guildId, String guildName, String expires) {}
}
