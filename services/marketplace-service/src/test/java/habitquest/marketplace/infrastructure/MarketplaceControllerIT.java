package habitquest.marketplace.infrastructure;

import static habitquest.marketplace.MarketplaceFixtures.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import habitquest.marketplace.application.AvatarNotFoundException;
import habitquest.marketplace.application.MarketplaceLogger;
import habitquest.marketplace.application.MarketplaceNotFoundException;
import habitquest.marketplace.application.MarketplaceService;
import habitquest.marketplace.domain.ItemCatalog;
import habitquest.marketplace.domain.ItemNotFoundException;
import habitquest.marketplace.domain.Marketplace;
import habitquest.marketplace.domain.Money;
import habitquest.marketplace.domain.items.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
  @MockitoBean private MarketplaceLogger log;

  private ItemCatalog catalog;

  @BeforeEach
  void setUp() {
    catalog = mockCatalog();
  }

  private Marketplace stubMarketplace() {
    return marketplace(catalog);
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
          .perform(get("/api/v1/marketplaces/{marketplaceId}", MARKETPLACE_ID.value()))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(marketplaceService.getMarketplace(UNKNOWN_MARKETPLACE_ID))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_MARKETPLACE_ID.value()));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}", UNKNOWN_MARKETPLACE_ID.value()))
          .andExpect(status().isNotFound());
    }
  }

  // ── POST /api/v1/marketplaces ─────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/marketplaces")
  class CreateMarketplace {

    @Test
    @DisplayName("returns 201 when marketplace is successfully created")
    void shouldReturn201WhenMarketplaceCreated() throws Exception {
      when(marketplaceService.createMarketplaceForAvatar(AVATAR_ID)).thenReturn(MARKETPLACE_ID);
      when(marketplaceService.getMarketplace(MARKETPLACE_ID)).thenReturn(stubMarketplace());

      String requestBody =
          objectMapper.writeValueAsString(
              new MarketplaceController.CreateMarketplaceRequest(AVATAR_ID.value()));

      mockMvc
          .perform(
              post("/api/v1/marketplaces").contentType("application/json").content(requestBody))
          .andExpect(status().isCreated());

      verify(marketplaceService).createMarketplaceForAvatar(AVATAR_ID);
    }

    @Test
    @DisplayName("returns 404 when avatar does not exist")
    void shouldReturn404WhenAvatarNotFound() throws Exception {
      when(marketplaceService.createMarketplaceForAvatar(AVATAR_ID))
          .thenThrow(new AvatarNotFoundException(AVATAR_ID.value()));

      String requestBody =
          objectMapper.writeValueAsString(
              new MarketplaceController.CreateMarketplaceRequest(AVATAR_ID.value()));

      mockMvc
          .perform(
              post("/api/v1/marketplaces").contentType("application/json").content(requestBody))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 502 when avatar service is unreachable")
    void shouldReturn502WhenAvatarServiceFails() throws Exception {
      when(marketplaceService.createMarketplaceForAvatar(AVATAR_ID))
          .thenThrow(
              new AvatarCommunicationException(
                  "Avatar service unavailable", new RestClientException("timeout")));

      String requestBody =
          objectMapper.writeValueAsString(
              new MarketplaceController.CreateMarketplaceRequest(AVATAR_ID.value()));

      mockMvc
          .perform(
              post("/api/v1/marketplaces").contentType("application/json").content(requestBody))
          .andExpect(status().isBadGateway());
    }
  }

  // ── GET /api/v1/marketplaces/{marketplaceId}/items ───────────────────────────

  @Nested
  @DisplayName("GET /api/v1/marketplaces/{marketplaceId}/items")
  class GetAvailableItems {

    @Test
    @DisplayName("returns 200 with all available items")
    void shouldReturn200WithAllItems() throws Exception {
      when(marketplaceService.getAllAvailableItems(MARKETPLACE_ID))
          .thenReturn(List.of(sword(), shield(), healthPotion(), manaPotion()));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID.value()))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with empty list when no items available")
    void shouldReturn200WithEmptyList() throws Exception {
      when(marketplaceService.getAllAvailableItems(MARKETPLACE_ID)).thenReturn(List.of());

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID.value()))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with armors when type=ARMOR")
    void shouldReturn200WithArmors() throws Exception {
      when(marketplaceService.getAvailableItemsByType(MARKETPLACE_ID, ItemType.ARMOR))
          .thenReturn(List.of(shield()));

      mockMvc
          .perform(
              get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID.value())
                  .param("type", "ARMOR"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with weapons when type=WEAPON")
    void shouldReturn200WithWeapons() throws Exception {
      when(marketplaceService.getAvailableItemsByType(MARKETPLACE_ID, ItemType.WEAPON))
          .thenReturn(List.of(sword()));

      mockMvc
          .perform(
              get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID.value())
                  .param("type", "WEAPON"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with potions when type=POTION")
    void shouldReturn200WithPotions() throws Exception {
      when(marketplaceService.getAvailableItemsByType(MARKETPLACE_ID, ItemType.POTION))
          .thenReturn(List.of(healthPotion(), manaPotion()));

      mockMvc
          .perform(
              get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID.value())
                  .param("type", "POTION"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with health potions when type=HEALTH_POTION")
    void shouldReturn200WithHealthPotions() throws Exception {
      when(marketplaceService.getAvailableItemsByType(MARKETPLACE_ID, ItemType.HEALTH_POTION))
          .thenReturn(List.of(healthPotion()));

      mockMvc
          .perform(
              get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID.value())
                  .param("type", "HEALTH_POTION"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with mana potions when type=MANA_POTION")
    void shouldReturn200WithManaPotions() throws Exception {
      when(marketplaceService.getAvailableItemsByType(MARKETPLACE_ID, ItemType.MANA_POTION))
          .thenReturn(List.of(manaPotion()));

      mockMvc
          .perform(
              get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID.value())
                  .param("type", "MANA_POTION"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(marketplaceService.getAllAvailableItems(UNKNOWN_MARKETPLACE_ID))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_MARKETPLACE_ID.value()));

      mockMvc
          .perform(
              get("/api/v1/marketplaces/{marketplaceId}/items", UNKNOWN_MARKETPLACE_ID.value()))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/marketplaces/{marketplaceId}/items/{itemName} ────────────────

  @Nested
  @DisplayName("GET /api/v1/marketplaces/{marketplaceId}/items/{itemName}")
  class GetAvailableItem {

    @Test
    @DisplayName("returns 200 with the requested item")
    void shouldReturn200WhenItemFound() throws Exception {
      when(marketplaceService.getAvailableItem(MARKETPLACE_ID, SWORD_NAME)).thenReturn(sword());

      mockMvc
          .perform(
              get(
                  "/api/v1/marketplaces/{marketplaceId}/items/{itemName}",
                  MARKETPLACE_ID.value(),
                  SWORD_NAME))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when item does not exist or is already bought")
    void shouldReturn404WhenItemNotFound() throws Exception {
      when(marketplaceService.getAvailableItem(MARKETPLACE_ID, UNKNOWN_ITEM))
          .thenThrow(new ItemNotFoundException(UNKNOWN_ITEM));

      mockMvc
          .perform(
              get(
                  "/api/v1/marketplaces/{marketplaceId}/items/{itemName}",
                  MARKETPLACE_ID.value(),
                  UNKNOWN_ITEM))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenMarketplaceNotFound() throws Exception {
      when(marketplaceService.getAvailableItem(UNKNOWN_MARKETPLACE_ID, SWORD_NAME))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_MARKETPLACE_ID.value()));

      mockMvc
          .perform(
              get(
                  "/api/v1/marketplaces/{marketplaceId}/items/{itemName}",
                  UNKNOWN_MARKETPLACE_ID.value(),
                  SWORD_NAME))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/marketplaces/{marketplaceId}/sold-items ──────────────────────

  @Nested
  @DisplayName("GET /api/v1/marketplaces/{marketplaceId}/sold-items")
  class GetSoldItems {

    @Test
    @DisplayName("returns 200 with all sold items")
    void shouldReturn200WithSoldItems() throws Exception {
      when(marketplaceService.getSoldItems(MARKETPLACE_ID)).thenReturn(List.of(sword(), shield()));

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/sold-items", MARKETPLACE_ID.value()))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with empty list when nothing has been bought")
    void shouldReturn200WithEmptyList() throws Exception {
      when(marketplaceService.getSoldItems(MARKETPLACE_ID)).thenReturn(List.of());

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/sold-items", MARKETPLACE_ID.value()))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(marketplaceService.getSoldItems(UNKNOWN_MARKETPLACE_ID))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_MARKETPLACE_ID.value()));

      mockMvc
          .perform(
              get(
                  "/api/v1/marketplaces/{marketplaceId}/sold-items",
                  UNKNOWN_MARKETPLACE_ID.value()))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/marketplaces/{marketplaceId}/sold-items/{itemName} ────────────

  @Nested
  @DisplayName("GET /api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}")
  class GetSoldItem {

    @Test
    @DisplayName("returns 200 with the requested sold item")
    void shouldReturn200WhenItemFound() throws Exception {
      when(marketplaceService.getSoldItem(MARKETPLACE_ID, SWORD_NAME)).thenReturn(sword());

      mockMvc
          .perform(
              get(
                  "/api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}",
                  MARKETPLACE_ID.value(),
                  SWORD_NAME))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when item has not been bought yet")
    void shouldReturn404WhenItemNotFound() throws Exception {
      when(marketplaceService.getSoldItem(MARKETPLACE_ID, UNKNOWN_ITEM))
          .thenThrow(new ItemNotFoundException(UNKNOWN_ITEM));

      mockMvc
          .perform(
              get(
                  "/api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}",
                  MARKETPLACE_ID.value(),
                  UNKNOWN_ITEM))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenMarketplaceNotFound() throws Exception {
      when(marketplaceService.getSoldItem(UNKNOWN_MARKETPLACE_ID, SWORD_NAME))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_MARKETPLACE_ID.value()));

      mockMvc
          .perform(
              get(
                  "/api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}",
                  UNKNOWN_MARKETPLACE_ID.value(),
                  SWORD_NAME))
          .andExpect(status().isNotFound());
    }
  }

  // ── POST /api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy ────────────

  @Nested
  @DisplayName("POST /api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy")
  class BuyItem {

    @Test
    @DisplayName("returns 204 when purchase is successful and level requirement is met")
    void shouldReturn204AndDelegateOnSuccess() throws Exception {
      when(marketplaceService.getAvatarId(MARKETPLACE_ID)).thenReturn(AVATAR_ID);
      when(marketplaceService.getAvailableItem(MARKETPLACE_ID, SWORD_NAME)).thenReturn(sword());
      when(marketplaceService.canBuyItem(eq(MARKETPLACE_ID), eq(SWORD_NAME), any(Level.class)))
          .thenReturn(true);
      doNothing().when(avatarClient).spendMoney(eq(AVATAR_ID.value()), any(Money.class));
      doNothing().when(avatarClient).addItemToInventory(eq(AVATAR_ID.value()), any(Item.class));
      doNothing().when(marketplaceService).buyItem(MARKETPLACE_ID, SWORD_NAME);

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      MARKETPLACE_ID.value(),
                      SWORD_NAME)
                  .param("currentLevel", "1"))
          .andExpect(status().isNoContent());

      verify(avatarClient).spendMoney(eq(AVATAR_ID.value()), any(Money.class));
      verify(avatarClient).addItemToInventory(eq(AVATAR_ID.value()), any(Item.class));
      verify(marketplaceService).buyItem(MARKETPLACE_ID, SWORD_NAME);
    }

    @Test
    @DisplayName("returns 403 when avatar level is below item requirement")
    void shouldReturn403WhenLevelInsufficient() throws Exception {
      when(marketplaceService.getAvatarId(MARKETPLACE_ID)).thenReturn(AVATAR_ID);
      when(marketplaceService.canBuyItem(eq(MARKETPLACE_ID), eq(SWORD_NAME), any(Level.class)))
          .thenReturn(false);

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      MARKETPLACE_ID.value(),
                      SWORD_NAME)
                  .param("currentLevel", "1"))
          .andExpect(status().isForbidden());

      verifyNoInteractions(avatarClient);
      verify(marketplaceService, never()).buyItem(any(), any());
    }

    @Test
    @DisplayName("does not interact with avatar client when level check fails")
    void shouldNotCallAvatarClientWhenLevelInsufficient() throws Exception {
      when(marketplaceService.getAvatarId(MARKETPLACE_ID)).thenReturn(AVATAR_ID);
      when(marketplaceService.canBuyItem(eq(MARKETPLACE_ID), eq(SWORD_NAME), any(Level.class)))
          .thenReturn(false);

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      MARKETPLACE_ID.value(),
                      SWORD_NAME)
                  .param("currentLevel", "1"))
          .andExpect(status().isForbidden());

      verifyNoInteractions(avatarClient);
    }

    @Test
    @DisplayName("returns 404 when item does not exist or is already bought")
    void shouldReturn404WhenItemNotFound() throws Exception {
      when(marketplaceService.getAvatarId(MARKETPLACE_ID)).thenReturn(AVATAR_ID);
      when(marketplaceService.canBuyItem(eq(MARKETPLACE_ID), eq(UNKNOWN_ITEM), any(Level.class)))
          .thenReturn(true);
      when(marketplaceService.getAvailableItem(MARKETPLACE_ID, UNKNOWN_ITEM))
          .thenThrow(new ItemNotFoundException(UNKNOWN_ITEM));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      MARKETPLACE_ID.value(),
                      UNKNOWN_ITEM)
                  .param("currentLevel", "1"))
          .andExpect(status().isNotFound());

      verifyNoInteractions(avatarClient);
    }

    @Test
    @DisplayName("returns 404 when avatar does not exist")
    void shouldReturn404WhenAvatarNotFound() throws Exception {
      when(marketplaceService.getAvatarId(MARKETPLACE_ID)).thenReturn(AVATAR_ID);
      when(marketplaceService.canBuyItem(eq(MARKETPLACE_ID), eq(SWORD_NAME), any(Level.class)))
          .thenReturn(true);
      when(marketplaceService.getAvailableItem(MARKETPLACE_ID, SWORD_NAME)).thenReturn(sword());
      doThrow(new AvatarNotFoundException(AVATAR_ID.value()))
          .when(avatarClient)
          .spendMoney(eq(AVATAR_ID.value()), any(Money.class));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      MARKETPLACE_ID.value(),
                      SWORD_NAME)
                  .param("currentLevel", "1"))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 502 when avatar service is unreachable")
    void shouldReturn502WhenAvatarServiceFails() throws Exception {
      when(marketplaceService.getAvatarId(MARKETPLACE_ID)).thenReturn(AVATAR_ID);
      when(marketplaceService.canBuyItem(eq(MARKETPLACE_ID), eq(SWORD_NAME), any(Level.class)))
          .thenReturn(true);
      when(marketplaceService.getAvailableItem(MARKETPLACE_ID, SWORD_NAME)).thenReturn(sword());
      doThrow(
              new AvatarCommunicationException(
                  "Avatar service unavailable", new RestClientException("timeout")))
          .when(avatarClient)
          .spendMoney(eq(AVATAR_ID.value()), any(Money.class));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      MARKETPLACE_ID.value(),
                      SWORD_NAME)
                  .param("currentLevel", "1"))
          .andExpect(status().isBadGateway());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenMarketplaceNotFound() throws Exception {
      when(marketplaceService.getAvatarId(UNKNOWN_MARKETPLACE_ID))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_MARKETPLACE_ID.value()));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      UNKNOWN_MARKETPLACE_ID.value(),
                      SWORD_NAME)
                  .param("currentLevel", "1"))
          .andExpect(status().isNotFound());
    }
  }

  // ── POST /api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}/sell ──────

  @Nested
  @DisplayName("POST /api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}/sell")
  class SellItem {

    @Test
    @DisplayName("returns 204 and delegates to service and avatar client on successful sale")
    void shouldReturn204AndDelegateOnSuccess() throws Exception {
      when(marketplaceService.getAvatarId(MARKETPLACE_ID)).thenReturn(AVATAR_ID);
      when(marketplaceService.getSoldItem(MARKETPLACE_ID, SWORD_NAME)).thenReturn(sword());
      doNothing()
          .when(avatarClient)
          .removeItemFromInventory(eq(AVATAR_ID.value()), any(Item.class));
      doNothing().when(avatarClient).earnMoney(eq(AVATAR_ID.value()), any(Money.class));
      doNothing().when(marketplaceService).sellItem(MARKETPLACE_ID, SWORD_NAME);

      mockMvc
          .perform(
              post(
                  "/api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}/sell",
                  MARKETPLACE_ID.value(),
                  SWORD_NAME))
          .andExpect(status().isNoContent());

      verify(avatarClient).removeItemFromInventory(eq(AVATAR_ID.value()), any(Item.class));
      verify(avatarClient).earnMoney(eq(AVATAR_ID.value()), any(Money.class));
      verify(marketplaceService).sellItem(MARKETPLACE_ID, SWORD_NAME);
    }

    @Test
    @DisplayName("returns 404 when item has not been bought yet")
    void shouldReturn404WhenItemNotFound() throws Exception {
      when(marketplaceService.getAvatarId(MARKETPLACE_ID)).thenReturn(AVATAR_ID);
      when(marketplaceService.getSoldItem(MARKETPLACE_ID, UNKNOWN_ITEM))
          .thenThrow(new ItemNotFoundException(UNKNOWN_ITEM));

      mockMvc
          .perform(
              post(
                  "/api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}/sell",
                  MARKETPLACE_ID.value(),
                  UNKNOWN_ITEM))
          .andExpect(status().isNotFound());

      verifyNoInteractions(avatarClient);
    }

    @Test
    @DisplayName("returns 404 when avatar does not exist")
    void shouldReturn404WhenAvatarNotFound() throws Exception {
      when(marketplaceService.getAvatarId(MARKETPLACE_ID)).thenReturn(AVATAR_ID);
      when(marketplaceService.getSoldItem(MARKETPLACE_ID, SWORD_NAME)).thenReturn(sword());
      doThrow(new AvatarNotFoundException(AVATAR_ID.value()))
          .when(avatarClient)
          .removeItemFromInventory(eq(AVATAR_ID.value()), any(Item.class));

      mockMvc
          .perform(
              post(
                  "/api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}/sell",
                  MARKETPLACE_ID.value(),
                  SWORD_NAME))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 502 when avatar service is unreachable")
    void shouldReturn502WhenAvatarServiceFails() throws Exception {
      when(marketplaceService.getAvatarId(MARKETPLACE_ID)).thenReturn(AVATAR_ID);
      when(marketplaceService.getSoldItem(MARKETPLACE_ID, SWORD_NAME)).thenReturn(sword());
      doThrow(
              new AvatarCommunicationException(
                  "Avatar service unavailable", new RestClientException("timeout")))
          .when(avatarClient)
          .removeItemFromInventory(eq(AVATAR_ID.value()), any(Item.class));

      mockMvc
          .perform(
              post(
                  "/api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}/sell",
                  MARKETPLACE_ID.value(),
                  SWORD_NAME))
          .andExpect(status().isBadGateway());
    }

    @Test
    @DisplayName("returns 404 when marketplace does not exist")
    void shouldReturn404WhenMarketplaceNotFound() throws Exception {
      when(marketplaceService.getAvatarId(UNKNOWN_MARKETPLACE_ID))
          .thenThrow(new MarketplaceNotFoundException(UNKNOWN_MARKETPLACE_ID.value()));

      mockMvc
          .perform(
              post(
                  "/api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}/sell",
                  UNKNOWN_MARKETPLACE_ID.value(),
                  SWORD_NAME))
          .andExpect(status().isNotFound());
    }
  }
}
