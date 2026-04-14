package habitquest.marketplace.infrastructure.outbound;

import common.hexagonal.Adapter;
import habitquest.marketplace.application.exceptions.AvatarCommunicationException;
import habitquest.marketplace.application.port.out.AvatarClientPort;
import habitquest.marketplace.application.port.out.MarketplaceLogger;
import habitquest.marketplace.domain.items.Item;
import habitquest.marketplace.domain.marketplace.Money;
import habitquest.marketplace.infrastructure.dto.ItemMapper;
import habitquest.marketplace.infrastructure.dto.MarketplaceRequestsDto.*;
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
  private final MarketplaceLogger log;

  public AvatarClient(RestClient avatarRestClient, MarketplaceLogger log) {
    this.restClient = avatarRestClient;
    this.log = log;
  }

  // ─── Money ──────────────────────────────────────────────────────────────────

  @CircuitBreaker(name = AVATAR_CLIENT, fallbackMethod = "spendMoneyFallback")
  @Retry(name = AVATAR_CLIENT)
  public void spendMoney(String avatarId, Money price) {
    AmountRequest request = new AmountRequest(price.amount());
    log.info(request, "Spending money for avatar " + avatarId);
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/money/spend", avatarId)
          .body(request)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException ex) {
      log.error(request, "Failed to spend money for avatar " + avatarId, ex);
      throw new AvatarCommunicationException("Could not deduct money from avatar " + avatarId, ex);
    }
  }

  @CircuitBreaker(name = AVATAR_CLIENT, fallbackMethod = "earnMoneyFallback")
  @Retry(name = AVATAR_CLIENT)
  public void earnMoney(String avatarId, Money price) {
    AmountRequest request = new AmountRequest(price.amount());
    log.info(request, "Crediting money to avatar " + avatarId);
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/money/earn", avatarId)
          .body(request)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException ex) {
      log.error(request, "Failed to credit money to avatar " + avatarId, ex);
      throw new AvatarCommunicationException("Could not credit money to avatar " + avatarId, ex);
    }
  }

  // ─── Inventory ──────────────────────────────────────────────────────────────

  @CircuitBreaker(name = AVATAR_CLIENT, fallbackMethod = "addItemToInventoryFallback")
  @Retry(name = AVATAR_CLIENT)
  public void addItemToInventory(String avatarId, Item item) {
    ItemRequest request = ItemMapper.from(item);
    log.info(request, "Adding item to inventory of avatar " + avatarId);
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/inventory/items", avatarId)
          .body(request)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException ex) {
      log.error(request, "Failed to add item to inventory of avatar " + avatarId, ex);
      throw new AvatarCommunicationException(
          "Could not add item to inventory of avatar " + avatarId, ex);
    }
  }

  @CircuitBreaker(name = AVATAR_CLIENT, fallbackMethod = "removeItemFromInventoryFallback")
  @Retry(name = AVATAR_CLIENT)
  public void removeItemFromInventory(String avatarId, Item item) {
    ItemRequest request = ItemMapper.from(item);
    log.info(request, "Removing item from inventory of avatar " + avatarId);
    try {
      restClient
          .method(org.springframework.http.HttpMethod.DELETE)
          .uri("/api/v1/avatars/{id}/inventory/items", avatarId)
          .body(request)
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException ex) {
      log.error(request, "Failed to remove item from inventory of avatar " + avatarId, ex);
      throw new AvatarCommunicationException(
          "Could not remove item from inventory of avatar " + avatarId, ex);
    }
  }

  // ─── Fallback methods ────────────────────────────────────────────────────────

  private void spendMoneyFallback(String avatarId, Money price, Exception ex) {
    log.error(
        new AmountRequest(price.amount()),
        "Circuit breaker OPEN per spendMoney, avatarId=" + avatarId,
        ex);
    throw new AvatarCommunicationException(
        "Avatar service non disponibile (spendMoney) per " + avatarId, ex);
  }

  private void earnMoneyFallback(String avatarId, Money price, Exception ex) {
    log.error(
        new AmountRequest(price.amount()),
        "Circuit breaker OPEN per earnMoney, avatarId=" + avatarId,
        ex);
    throw new AvatarCommunicationException(
        "Avatar service non disponibile (earnMoney) per " + avatarId, ex);
  }

  private void addItemToInventoryFallback(String avatarId, Item item, Exception ex) {
    log.error(
        ItemMapper.from(item),
        "Circuit breaker OPEN per addItemToInventory, avatarId=" + avatarId,
        ex);
    throw new AvatarCommunicationException(
        "Avatar service non disponibile (addItem) per " + avatarId, ex);
  }

  private void removeItemFromInventoryFallback(String avatarId, Item item, Exception ex) {
    log.error(
        ItemMapper.from(item),
        "Circuit breaker OPEN per removeItemFromInventory, avatarId=" + avatarId,
        ex);
    throw new AvatarCommunicationException(
        "Avatar service non disponibile (removeItem) per " + avatarId, ex);
  }

  // ─── Request records ────────────────────────────────────────────────────────
  record AmountRequest(Integer amount) {}
}
