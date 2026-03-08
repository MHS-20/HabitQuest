package habitquest.marketplace.infrastructure;

import common.hexagonal.Adapter;
import habitquest.marketplace.domain.Money;
import habitquest.marketplace.domain.items.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@Component
public class AvatarClient {

  private static final Logger LOG = LoggerFactory.getLogger(AvatarClient.class);

  private final RestClient restClient;

  public AvatarClient(RestClient avatarRestClient) {
    this.restClient = avatarRestClient;
  }

  // ─── Money ──────────────────────────────────────────────────────────────────
  public void spendMoney(String avatarId, Money price) {
    LOG.info("Spending {} gold for avatar {}", price.amount(), avatarId);
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/money/spend", avatarId)
          .body(new AmountRequest(price.amount()))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException ex) {
      LOG.error("Failed to spend money for avatar {}: {}", avatarId, ex.getMessage());
      throw new AvatarCommunicationException("Could not deduct money from avatar " + avatarId, ex);
    }
  }

  public void earnMoney(String avatarId, Money price) {
    LOG.info("Crediting {} gold to avatar {}", price.amount(), avatarId);
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/money/earn", avatarId)
          .body(new AmountRequest(price.amount()))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException ex) {
      LOG.error("Failed to earn money for avatar {}: {}", avatarId, ex.getMessage());
      throw new AvatarCommunicationException("Could not credit money to avatar " + avatarId, ex);
    }
  }

  // ─── Inventory ──────────────────────────────────────────────────────────────
  public void addItemToInventory(String avatarId, Item item) {
    LOG.info("Adding item '{}' to inventory of avatar {}", item.name(), avatarId);
    try {
      restClient
          .post()
          .uri("/api/v1/avatars/{id}/inventory/items", avatarId)
          .body(ItemRequest.from(item))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException ex) {
      LOG.error(
          "Failed to add item '{}' for avatar {}: {}", item.name(), avatarId, ex.getMessage());
      throw new AvatarCommunicationException(
          "Could not add item to inventory of avatar " + avatarId, ex);
    }
  }

  public void removeItemFromInventory(String avatarId, Item item) {
    LOG.info("Removing item '{}' from inventory of avatar {}", item.name(), avatarId);
    try {
      restClient
          .method(org.springframework.http.HttpMethod.DELETE)
          .uri("/api/v1/avatars/{id}/inventory/items", avatarId)
          .body(ItemRequest.from(item))
          .retrieve()
          .toBodilessEntity();
    } catch (RestClientException ex) {
      LOG.error(
          "Failed to remove item '{}' for avatar {}: {}", item.name(), avatarId, ex.getMessage());
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
