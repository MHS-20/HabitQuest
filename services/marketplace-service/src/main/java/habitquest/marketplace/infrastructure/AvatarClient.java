package habitquest.marketplace.infrastructure;

import common.hexagonal.Adapter;
import habitquest.marketplace.application.MarketplaceLogger;
import habitquest.marketplace.domain.Money;
import habitquest.marketplace.domain.items.Item;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@Component
public class AvatarClient {

  private final RestClient restClient;
  private final MarketplaceLogger log;

  public AvatarClient(RestClient avatarRestClient, MarketplaceLogger log) {
    this.restClient = avatarRestClient;
    this.log = log;
  }

  // ─── Money ──────────────────────────────────────────────────────────────────

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

  public void addItemToInventory(String avatarId, Item item) {
    ItemRequest request = ItemRequest.from(item);
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

  public void removeItemFromInventory(String avatarId, Item item) {
    ItemRequest request = ItemRequest.from(item);
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

  // ─── Request records ────────────────────────────────────────────────────────

  record AmountRequest(Integer amount) {}

  record ItemRequest(String type, String name, String description, Integer power) {

    static ItemRequest from(Item item) {
      return switch (item) {
        case habitquest.marketplace.domain.items.Weapon w ->
            new ItemRequest("WEAPON", w.name(), w.description(), w.attackPower());
        case habitquest.marketplace.domain.items.Armor a ->
            new ItemRequest("ARMOR", a.name(), a.description(), a.defensePower());
        case habitquest.marketplace.domain.items.HealthPotion hp ->
            new ItemRequest("HEALTH_POTION", hp.name(), hp.description(), hp.healingPower());
        case habitquest.marketplace.domain.items.ManaPotion mp ->
            new ItemRequest("MANA_POTION", mp.name(), mp.description(), mp.restoringPower());
        default -> new ItemRequest("ITEM", item.name(), item.description(), 0);
      };
    }
  }
}
