package habitquest.marketplace.infrastructure;

import static habitquest.marketplace.MarketplaceFixtures.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import habitquest.marketplace.MarketplaceFixtures;
import habitquest.marketplace.application.exceptions.AvatarCommunicationException;
import habitquest.marketplace.application.exceptions.AvatarNotFoundException;
import habitquest.marketplace.application.exceptions.InsufficientLevelException;
import habitquest.marketplace.application.exceptions.MarketplaceNotFoundException;
import habitquest.marketplace.application.port.in.MarketplaceService;
import habitquest.marketplace.application.port.out.MarketplaceLogger;
import habitquest.marketplace.domain.exceptions.ItemNotFoundException;
import habitquest.marketplace.domain.items.*;
import habitquest.marketplace.domain.items.ItemCatalog;
import habitquest.marketplace.domain.marketplace.Marketplace;
import habitquest.marketplace.infrastructure.dto.ItemMapper;
import habitquest.marketplace.infrastructure.dto.MarketplaceRequestsDto.*;
import habitquest.marketplace.infrastructure.dto.MarketplaceResponseAssembler;
import habitquest.marketplace.infrastructure.dto.MarketplaceResponsesDto.*;
import habitquest.marketplace.infrastructure.inbound.MarketplaceController;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
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
  @MockitoBean private MarketplaceLogger log;
  @MockitoBean private MarketplaceResponseAssembler assembler;

  private ItemCatalog catalog;

  @BeforeEach
  void setUp() {
    catalog = mockCatalog();
  }

  private Marketplace stubMarketplace() {
    return marketplace(catalog);
  }

  private EntityModel<MarketplaceResponse> stubMarketplaceModel() {
    return EntityModel.of(MarketplaceFixtures.marketplaceResponse());
  }

  private EntityModel<ItemResponse> stubItemModel(Item item) {
    return EntityModel.of(ItemMapper.toResponse(item));
  }

  // ── GET /api/v1/marketplaces/{marketplaceId} ──────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/marketplaces/{marketplaceId}")
  class GetMarketplace {

    @Test
    @DisplayName("returns 200 with marketplace data when found")
    void shouldReturn200WhenFound() throws Exception {
      Marketplace marketplace = stubMarketplace();
      when(marketplaceService.getMarketplace(MARKETPLACE_ID)).thenReturn(marketplace);
      when(assembler.toModel(marketplace)).thenReturn(stubMarketplaceModel());

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

  // ── GET /api/v1/marketplaces/by-avatar/{avatarId} ─────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/marketplaces/by-avatar/{avatarId}")
  class GetMarketplaceByAvatarId {

    @Test
    @DisplayName("returns 200 and redirects to marketplace resource")
    void shouldReturn200WhenFoundByAvatar() throws Exception {
      Marketplace marketplace = stubMarketplace();
      when(marketplaceService.getMarketplaceIdByAvatarId(AVATAR_ID)).thenReturn(MARKETPLACE_ID);
      when(marketplaceService.getMarketplace(MARKETPLACE_ID)).thenReturn(marketplace);
      when(assembler.toModel(marketplace)).thenReturn(stubMarketplaceModel());

      mockMvc
          .perform(get("/api/v1/marketplaces/by-avatar/{avatarId}", AVATAR_ID.value()))
          .andExpect(status().isOk());
    }
  }

  // ── POST /api/v1/marketplaces ─────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/marketplaces")
  class CreateMarketplace {

    @Test
    @DisplayName("returns 201 when marketplace is successfully created")
    void shouldReturn201WhenMarketplaceCreated() throws Exception {
      Marketplace marketplace = stubMarketplace();
      when(marketplaceService.createMarketplaceForAvatar(AVATAR_ID)).thenReturn(MARKETPLACE_ID);
      when(marketplaceService.getMarketplace(MARKETPLACE_ID)).thenReturn(marketplace);
      when(assembler.toModel(marketplace)).thenReturn(stubMarketplaceModel());

      String requestBody =
          objectMapper.writeValueAsString(new CreateMarketplaceRequest(AVATAR_ID.value()));

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
          objectMapper.writeValueAsString(new CreateMarketplaceRequest(AVATAR_ID.value()));

      mockMvc
          .perform(
              post("/api/v1/marketplaces").contentType("application/json").content(requestBody))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 502 when avatar service is unreachable")
    void shouldReturn502WhenAvatarServiceFails() throws Exception {
      when(marketplaceService.createMarketplaceForAvatar(AVATAR_ID))
          .thenThrow(new AvatarCommunicationException("timeout", new RestClientException("err")));

      String requestBody =
          objectMapper.writeValueAsString(new CreateMarketplaceRequest(AVATAR_ID.value()));

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
      List<Item> items = List.of(sword(), shield());
      when(marketplaceService.getAllAvailableItems(MARKETPLACE_ID)).thenReturn(items);
      when(assembler.toAvailableItemsCollection(MARKETPLACE_ID.value(), items, ItemFilter.ALL))
          .thenReturn(CollectionModel.empty());

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID.value()))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with empty list when no items available")
    void shouldReturn200WithEmptyList() throws Exception {
      when(marketplaceService.getAllAvailableItems(MARKETPLACE_ID)).thenReturn(List.of());
      when(assembler.toAvailableItemsCollection(eq(MARKETPLACE_ID.value()), anyList(), any()))
          .thenReturn(CollectionModel.empty());

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID.value()))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with armors when type=ARMOR")
    void shouldReturn200WithArmors() throws Exception {
      List<Item> items = List.of(shield());
      when(marketplaceService.getAvailableItemsByType(MARKETPLACE_ID, ItemFilter.ARMOR))
          .thenReturn(items);
      when(assembler.toAvailableItemsCollection(MARKETPLACE_ID.value(), items, ItemFilter.ARMOR))
          .thenReturn(CollectionModel.empty());

      mockMvc
          .perform(
              get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID.value())
                  .param("type", "ARMOR"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with weapons when type=WEAPON")
    void shouldReturn200WithWeapons() throws Exception {
      List<Item> items = List.of(sword());
      when(marketplaceService.getAvailableItemsByType(MARKETPLACE_ID, ItemFilter.WEAPON))
          .thenReturn(items);
      when(assembler.toAvailableItemsCollection(MARKETPLACE_ID.value(), items, ItemFilter.WEAPON))
          .thenReturn(CollectionModel.empty());

      mockMvc
          .perform(
              get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID.value())
                  .param("type", "WEAPON"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with potions when type=POTION")
    void shouldReturn200WithPotions() throws Exception {
      List<Item> items = List.of(healthPotion(), manaPotion());
      when(marketplaceService.getAvailableItemsByType(MARKETPLACE_ID, ItemFilter.POTION))
          .thenReturn(items);
      when(assembler.toAvailableItemsCollection(MARKETPLACE_ID.value(), items, ItemFilter.POTION))
          .thenReturn(CollectionModel.empty());

      mockMvc
          .perform(
              get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID.value())
                  .param("type", "POTION"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with health potions when type=HEALTH_POTION")
    void shouldReturn200WithHealthPotions() throws Exception {
      List<Item> items = List.of(healthPotion());
      when(marketplaceService.getAvailableItemsByType(MARKETPLACE_ID, ItemFilter.HEALTH_POTION))
          .thenReturn(items);
      when(assembler.toAvailableItemsCollection(
              MARKETPLACE_ID.value(), items, ItemFilter.HEALTH_POTION))
          .thenReturn(CollectionModel.empty());

      mockMvc
          .perform(
              get("/api/v1/marketplaces/{marketplaceId}/items", MARKETPLACE_ID.value())
                  .param("type", "HEALTH_POTION"))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with mana potions when type=MANA_POTION")
    void shouldReturn200WithManaPotions() throws Exception {
      List<Item> items = List.of(manaPotion());
      when(marketplaceService.getAvailableItemsByType(MARKETPLACE_ID, ItemFilter.MANA_POTION))
          .thenReturn(items);
      when(assembler.toAvailableItemsCollection(
              MARKETPLACE_ID.value(), items, ItemFilter.MANA_POTION))
          .thenReturn(CollectionModel.empty());

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
      Item item = sword();
      when(marketplaceService.getAvailableItem(MARKETPLACE_ID, SWORD_NAME)).thenReturn(item);
      when(assembler.toAvailableItemModel(MARKETPLACE_ID.value(), item))
          .thenReturn(stubItemModel(item));

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
      List<Item> items = List.of(sword(), shield());
      when(marketplaceService.getSoldItems(MARKETPLACE_ID)).thenReturn(items);
      when(assembler.toSoldItemsCollection(MARKETPLACE_ID.value(), items))
          .thenReturn(CollectionModel.empty());

      mockMvc
          .perform(get("/api/v1/marketplaces/{marketplaceId}/sold-items", MARKETPLACE_ID.value()))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 200 with empty list when nothing has been bought")
    void shouldReturn200WithEmptyList() throws Exception {
      when(marketplaceService.getSoldItems(MARKETPLACE_ID)).thenReturn(List.of());
      when(assembler.toSoldItemsCollection(eq(MARKETPLACE_ID.value()), anyList()))
          .thenReturn(CollectionModel.empty());

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
      Item item = sword();
      when(marketplaceService.getSoldItem(MARKETPLACE_ID, SWORD_NAME)).thenReturn(item);
      when(assembler.toSoldItemModel(MARKETPLACE_ID.value(), item)).thenReturn(stubItemModel(item));

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
    @DisplayName("returns 204 when purchase is successful")
    void shouldReturn204OnSuccess() throws Exception {
      doNothing()
          .when(marketplaceService)
          .buyItem(eq(MARKETPLACE_ID), eq(SWORD_NAME), any(Level.class));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      MARKETPLACE_ID.value(),
                      SWORD_NAME)
                  .param("currentLevel", "1"))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("returns 403 when avatar level is below item requirement")
    void shouldReturn403WhenLevelInsufficient() throws Exception {
      doThrow(new InsufficientLevelException(SWORD_NAME))
          .when(marketplaceService)
          .buyItem(eq(MARKETPLACE_ID), eq(SWORD_NAME), any(Level.class));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      MARKETPLACE_ID.value(),
                      SWORD_NAME)
                  .param("currentLevel", "1"))
          .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("returns 404 when item does not exist or is already bought")
    void shouldReturn404WhenItemNotFound() throws Exception {
      doThrow(new ItemNotFoundException(UNKNOWN_ITEM))
          .when(marketplaceService)
          .buyItem(eq(MARKETPLACE_ID), eq(UNKNOWN_ITEM), any(Level.class));

      mockMvc
          .perform(
              post(
                      "/api/v1/marketplaces/{marketplaceId}/items/{itemName}/buy",
                      MARKETPLACE_ID.value(),
                      UNKNOWN_ITEM)
                  .param("currentLevel", "1"))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 502 when avatar service is unreachable")
    void shouldReturn502WhenAvatarServiceFails() throws Exception {
      doThrow(new AvatarCommunicationException("fail", new RestClientException("err")))
          .when(marketplaceService)
          .buyItem(eq(MARKETPLACE_ID), eq(SWORD_NAME), any(Level.class));

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
      doThrow(new MarketplaceNotFoundException(UNKNOWN_MARKETPLACE_ID.value()))
          .when(marketplaceService)
          .buyItem(eq(UNKNOWN_MARKETPLACE_ID), eq(SWORD_NAME), any(Level.class));

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
    @DisplayName("returns 204 on successful sale")
    void shouldReturn204OnSuccess() throws Exception {
      doNothing().when(marketplaceService).sellItem(MARKETPLACE_ID, SWORD_NAME);

      mockMvc
          .perform(
              post(
                  "/api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}/sell",
                  MARKETPLACE_ID.value(),
                  SWORD_NAME))
          .andExpect(status().isNoContent());

      verify(marketplaceService).sellItem(MARKETPLACE_ID, SWORD_NAME);
    }

    @Test
    @DisplayName("returns 404 when item has not been bought yet")
    void shouldReturn404WhenItemNotFound() throws Exception {
      doThrow(new ItemNotFoundException(UNKNOWN_ITEM))
          .when(marketplaceService)
          .sellItem(MARKETPLACE_ID, UNKNOWN_ITEM);

      mockMvc
          .perform(
              post(
                  "/api/v1/marketplaces/{marketplaceId}/sold-items/{itemName}/sell",
                  MARKETPLACE_ID.value(),
                  UNKNOWN_ITEM))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("returns 502 when avatar service is unreachable")
    void shouldReturn502WhenAvatarServiceFails() throws Exception {
      doThrow(new AvatarCommunicationException("fail", new RestClientException("err")))
          .when(marketplaceService)
          .sellItem(MARKETPLACE_ID, SWORD_NAME);

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
      doThrow(new MarketplaceNotFoundException(UNKNOWN_MARKETPLACE_ID.value()))
          .when(marketplaceService)
          .sellItem(UNKNOWN_MARKETPLACE_ID, SWORD_NAME);

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
