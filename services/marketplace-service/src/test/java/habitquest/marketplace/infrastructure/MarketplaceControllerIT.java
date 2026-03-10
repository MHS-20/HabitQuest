package habitquest.marketplace.infrastructure;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import habitquest.marketplace.application.AvatarNotFoundExpection;
import habitquest.marketplace.application.ItemNotFoundException;
import habitquest.marketplace.application.MarketplaceNotFoundException;
import habitquest.marketplace.application.MarketplaceService;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.MarketplaceImpl;
import habitquest.marketplace.domain.Money;
import habitquest.marketplace.domain.items.Armor;
import habitquest.marketplace.domain.items.HealthPotion;
import habitquest.marketplace.domain.items.Item;
import habitquest.marketplace.domain.items.Level;
import habitquest.marketplace.domain.items.ManaPotion;
import habitquest.marketplace.domain.items.Weapon;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;

@WebMvcTest(MarketplaceController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@DisplayName("MarketplaceController")
public class MarketplaceControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private MarketplaceService marketplaceService;
  @MockitoBean private AvatarClient avatarClient;

  // ── Fixtures ──────────────────────────────────────────────────────────────────

  private static final String MARKETPLACE_ID = "marketplace-1";
  private static final String AVATAR_ID = "avatar-1";
  private static final String UNKNOWN_ID = "ghost-99";

  private static final String SWORD_NAME = "Iron Sword";
  private static final String SHIELD_NAME = "Iron Shield";
  private static final String HEALTH_POTION_NAME = "Health Potion";
  private static final String MANA_POTION_NAME = "Mana Potion";
  private static final String UNKNOWN_ITEM = "Ghost Item";

  private static final Level LEVEL_1 = new Level(1);

  private Marketplace stubMarketplace() {
    return new MarketplaceImpl(
        MARKETPLACE_ID,
        List.of(stubWeapon(), stubArmor(), stubHealthPotion(), stubManaPotion()),
        List.of(stubArmor()),
        List.of(stubWeapon()),
        List.of(stubHealthPotion(), stubManaPotion()),
        List.of(stubHealthPotion()),
        List.of(stubManaPotion()));
  }

  private Weapon stubWeapon() {
    return new Weapon(SWORD_NAME, "A sturdy iron sword", 30, new Money(50), LEVEL_1);
  }

  private Armor stubArmor() {
    return new Armor(SHIELD_NAME, "A sturdy iron shield", 20, new Money(40), LEVEL_1);
  }

  private HealthPotion stubHealthPotion() {
    return new HealthPotion(HEALTH_POTION_NAME, "Restores health", 50, new Money(10), LEVEL_1);
  }

  private ManaPotion stubManaPotion() {
    return new ManaPotion(MANA_POTION_NAME, "Restores mana", 50, new Money(10), LEVEL_1);
  }

  // ── GET /api/v1/marketplaces/{marketplaceId} ──────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/marketplaces/{marketplaceId}")
  class GetMarketplace {

    @Test
    @DisplayName("returns 200 with marketplace data when found")
    void shouldReturn200WhenFound() throws Exception {
      when(marketplaceService.getMarketplace(MARKETPLACE_ID)).thenReturn(stubMarketplace());

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}", MARKETPLACE_ID))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(marketplaceService.getMarketplace(UNKNOWN_ID))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/marketplaces/{marketplaceId}/items ───────────────────────────

  @Nested
  @DisplayName("GET /api/v1/marketplaces/{marketplaceId}/items")
  class GetItems {

    @Test
    @DisplayName("returns 200 with all items in the marketplace")
    void shouldReturn200WithAllItems() throws Exception {
      when(marketplaceService.getItems(MARKETPLACE_ID))
          .thenReturn(List.of(stubWeapon(), stubArmor(), stubHealthPotion(), stubManaPotion()));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with empty list when marketplace has no items")
    void shouldReturn200WithEmptyList() throws Exception {
      when(marketplaceService.getItems(MARKETPLACE_ID)).thenReturn(List.of());

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(marketplaceService.getItems(UNKNOWN_ID))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/marketplaces/{marketplaceId}/items/{itemName} ────────────────

  @Nested
  @DisplayName("GET /api/v1/marketplaces/{marketplaceId}/items/{itemName}")
  class GetItem {

    @Test
    @DisplayName("returns 200 with the requested item")
    void shouldReturn200WhenItemFound() throws Exception {
      when(marketplaceService.getItemByName(MARKETPLACE_ID, SWORD_NAME)).thenReturn(stubWeapon());

      mockMvc
          .perform(
              get(
                  "/api/v1/marketplaces/{marketplaceId}/items/{itemName}",
                  MARKETPLACE_ID,
                  SWORD_NAME))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when item does not exist")
    void shouldReturn404WhenItemNotFound() throws Exception {
      when(marketplaceService.getItemByName(MARKETPLACE_ID, UNKNOWN_ITEM))
          .thenThrow(new ItemNotFoundException(MARKETPLACE_ID, UNKNOWN_ITEM));

      mockMvc
          .perform(
              get(
                  "/api/v1/marketplaces/{marketplaceId}/items/{itemName}",
                  MARKETPLACE_ID,
                  UNKNOWN_ITEM))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenMarketplaceNotFound() throws Exception {
      when(marketplaceService.getItemByName(UNKNOWN_ID, SWORD_NAME))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(
              get("/api/v1/marketplaces/{marketplaceId}/items/{itemName}", UNKNOWN_ID, SWORD_NAME))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/marketplaces/{marketplaceId}/items/armors ────────────────────

  @Nested
  @DisplayName("GET /api/v1/marketplaces/{marketplaceId}/items/armors")
  class GetArmors {

    @Test
    @DisplayName("returns 200 with all armors")
    void shouldReturn200WithArmors() throws Exception {
      when(marketplaceService.getArmors(MARKETPLACE_ID)).thenReturn(List.of(stubArmor()));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items/armors", MARKETPLACE_ID))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(marketplaceService.getArmors(UNKNOWN_ID))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items/armors", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/marketplaces/{marketplaceId}/items/weapons ───────────────────

  @Nested
  @DisplayName("GET /api/v1/marketplaces/{marketplaceId}/items/weapons")
  class GetWeapons {

    @Test
    @DisplayName("returns 200 with all weapons")
    void shouldReturn200WithWeapons() throws Exception {
      when(marketplaceService.getWeapons(MARKETPLACE_ID)).thenReturn(List.of(stubWeapon()));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items/weapons", MARKETPLACE_ID))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(marketplaceService.getWeapons(UNKNOWN_ID))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items/weapons", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/marketplaces/{marketplaceId}/items/potions ───────────────────

  @Nested
  @DisplayName("GET /api/v1/marketplaces/{marketplaceId}/items/potions")
  class GetPotions {

    @Test
    @DisplayName("returns 200 with all potions")
    void shouldReturn200WithPotions() throws Exception {
      when(marketplaceService.getPotions(MARKETPLACE_ID))
          .thenReturn(List.of(stubHealthPotion(), stubManaPotion()));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items/potions", MARKETPLACE_ID))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(marketplaceService.getPotions(UNKNOWN_ID))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items/potions", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/marketplaces/{marketplaceId}/items/potions/health ─────────────

  @Nested
  @DisplayName("GET /api/v1/marketplaces/{marketplaceId}/items/potions/health")
  class GetHealthPotions {

    @Test
    @DisplayName("returns 200 with all health potions")
    void shouldReturn200WithHealthPotions() throws Exception {
      when(marketplaceService.getHealthPotions(MARKETPLACE_ID))
          .thenReturn(List.of(stubHealthPotion()));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items/potions/health", MARKETPLACE_ID))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(marketplaceService.getHealthPotions(UNKNOWN_ID))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items/potions/health", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/marketplaces/{marketplaceId}/items/potions/mana ──────────────

  @Nested
  @DisplayName("GET /api/v1/marketplaces/{marketplaceId}/items/potions/mana")
  class GetManaPotions {

    @Test
    @DisplayName("returns 200 with all mana potions")
    void shouldReturn200WithManaPotions() throws Exception {
      when(marketplaceService.getManaPotions(MARKETPLACE_ID)).thenReturn(List.of(stubManaPotion()));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items/potions/mana", MARKETPLACE_ID))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(marketplaceService.getManaPotions(UNKNOWN_ID))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items/potions/mana", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── POST /api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy ────────────

  @Nested
  @DisplayName("POST /api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy")
  class BuyItem {

    @Test
    @DisplayName("returns 204 and delegates to service and avatar client on successful purchase")
    void shouldReturn204AndDelegateOnSuccess() throws Exception {
      when(marketplaceService.getItemByName(MARKETPLACE_ID, SWORD_NAME)).thenReturn(stubWeapon());
      doNothing().when(avatarClient).spendMoney(eq(AVATAR_ID), any(Money.class));
      doNothing().when(avatarClient).addItemToInventory(eq(AVATAR_ID), any(Item.class));
      doNothing().when(marketplaceService).buyItem(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID);

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      MARKETPLACE_ID,
                      SWORD_NAME)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"avatarId":"avatar-1"}
                      """))
          .andExpect(status().isNoContent());

      verify(avatarClient).spendMoney(eq(AVATAR_ID), any(Money.class));
      verify(avatarClient).addItemToInventory(eq(AVATAR_ID), any(Item.class));
      verify(marketplaceService).buyItem(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID);
    }

    @Test
    @DisplayName("returns 404 when item does not exist")
    void shouldReturn404WhenItemNotFound() throws Exception {
      when(marketplaceService.getItemByName(MARKETPLACE_ID, UNKNOWN_ITEM))
          .thenThrow(new ItemNotFoundException(MARKETPLACE_ID, UNKNOWN_ITEM));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      MARKETPLACE_ID,
                      UNKNOWN_ITEM)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"avatarId":"avatar-1"}
                      """))
          .andExpect(status().isNotFound());

      verifyNoInteractions(avatarClient);
    }

    @Test
    @DisplayName("returns 404 when avatar does not exist")
    void shouldReturn404WhenAvatarNotFound() throws Exception {
      when(marketplaceService.getItemByName(MARKETPLACE_ID, SWORD_NAME)).thenReturn(stubWeapon());
      doThrow(new AvatarNotFoundExpection(AVATAR_ID))
          .when(avatarClient)
          .spendMoney(eq(AVATAR_ID), any(Money.class));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      MARKETPLACE_ID,
                      SWORD_NAME)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"avatarId":"avatar-1"}
                      """))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 502 when avatar service is unreachable")
    void shouldReturn502WhenAvatarServiceFails() throws Exception {
      when(marketplaceService.getItemByName(MARKETPLACE_ID, SWORD_NAME)).thenReturn(stubWeapon());
      doThrow(
              new AvatarCommunicationException(
                  "Avatar service unavailable", new RestClientException("timeout")))
          .when(avatarClient)
          .spendMoney(eq(AVATAR_ID), any(Money.class));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      MARKETPLACE_ID,
                      SWORD_NAME)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"avatarId":"avatar-1"}
                      """))
          .andExpect(status().isBadGateway());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenMarketplaceNotFound() throws Exception {
      when(marketplaceService.getItemByName(UNKNOWN_ID, SWORD_NAME))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      UNKNOWN_ID,
                      SWORD_NAME)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"avatarId":"avatar-1"}
                      """))
          .andExpect(status().isNotFound());
    }
  }

  // ── POST /api/v1/marketplaces/{marketplaceId}/items/{itemName}/sell ───────────

  @Nested
  @DisplayName("POST /api/v1/marketplaces/{marketplaceId}/items/{itemName}/sell")
  class SellItem {

    @Test
    @DisplayName("returns 204 and delegates to service and avatar client on successful sale")
    void shouldReturn204AndDelegateOnSuccess() throws Exception {
      when(marketplaceService.getItemByName(MARKETPLACE_ID, SWORD_NAME)).thenReturn(stubWeapon());
      doNothing().when(avatarClient).removeItemFromInventory(eq(AVATAR_ID), any(Item.class));
      doNothing().when(avatarClient).earnMoney(eq(AVATAR_ID), any(Money.class));
      doNothing().when(marketplaceService).sellItem(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID);

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/sell",
                      MARKETPLACE_ID,
                      SWORD_NAME)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"avatarId":"avatar-1"}
                      """))
          .andExpect(status().isNoContent());

      verify(avatarClient).removeItemFromInventory(eq(AVATAR_ID), any(Item.class));
      verify(avatarClient).earnMoney(eq(AVATAR_ID), any(Money.class));
      verify(marketplaceService).sellItem(MARKETPLACE_ID, SWORD_NAME, AVATAR_ID);
    }

    @Test
    @DisplayName("returns 404 when item does not exist")
    void shouldReturn404WhenItemNotFound() throws Exception {
      when(marketplaceService.getItemByName(MARKETPLACE_ID, UNKNOWN_ITEM))
          .thenThrow(new ItemNotFoundException(MARKETPLACE_ID, UNKNOWN_ITEM));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/sell",
                      MARKETPLACE_ID,
                      UNKNOWN_ITEM)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"avatarId":"avatar-1"}
                      """))
          .andExpect(status().isNotFound());

      verifyNoInteractions(avatarClient);
    }

    @Test
    @DisplayName("returns 404 when avatar does not exist")
    void shouldReturn404WhenAvatarNotFound() throws Exception {
      when(marketplaceService.getItemByName(MARKETPLACE_ID, SWORD_NAME)).thenReturn(stubWeapon());
      doThrow(new AvatarNotFoundExpection(AVATAR_ID))
          .when(avatarClient)
          .removeItemFromInventory(eq(AVATAR_ID), any(Item.class));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/sell",
                      MARKETPLACE_ID,
                      SWORD_NAME)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"avatarId":"avatar-1"}
                      """))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 502 when avatar service is unreachable")
    void shouldReturn502WhenAvatarServiceFails() throws Exception {
      when(marketplaceService.getItemByName(MARKETPLACE_ID, SWORD_NAME)).thenReturn(stubWeapon());
      doThrow(
              new AvatarCommunicationException(
                  "Avatar service unavailable", new RestClientException("timeout")))
          .when(avatarClient)
          .removeItemFromInventory(eq(AVATAR_ID), any(Item.class));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/sell",
                      MARKETPLACE_ID,
                      SWORD_NAME)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"avatarId":"avatar-1"}
                      """))
          .andExpect(status().isBadGateway());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenMarketplaceNotFound() throws Exception {
      when(marketplaceService.getItemByName(UNKNOWN_ID, SWORD_NAME))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/sell",
                      UNKNOWN_ID,
                      SWORD_NAME)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"avatarId":"avatar-1"}
                      """))
          .andExpect(status().isNotFound());
    }
  }
}
