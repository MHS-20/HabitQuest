package habitquest.avatar.infrastructure;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.ddd.Id;
import habitquest.avatar.application.AvatarNotFoundException;
import habitquest.avatar.application.AvatarService;
import habitquest.avatar.domain.avatar.*;
import habitquest.avatar.domain.items.*;
import habitquest.avatar.domain.stats.AvatarStats;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AvatarController.class)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@DisplayName("AvatarController")
public class AvatarControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private MarketplaceClient marketplaceClient;
  @MockitoBean private AvatarService avatarService;

  // ── Fixtures ─────────────────────────────────────────────────────────────────

  private static final String AVATAR_ID = "avatar-1";
  private static final String AVATAR_NAME = "Hero";
  private static final String UNKNOWN_ID = "ghost-99";

  private Avatar stubAvatar() {
    return new Avatar(
        new Id<>(AVATAR_ID),
        AVATAR_NAME,
        new Money(0),
        new Inventory(new Id<>("inv-1")),
        new EquippedItems(new Id<>("equip-1")),
        new Level(1, new Experience(0), new Experience(100)),
        new AvatarHealth(new Health(100), new Health(100)),
        new AvatarMana(new Mana(50), new Mana(50)),
        new AvatarStats(new Id<>("stats-1"), 10, 10, 10),
        List.of());
  }

  // ── POST /api/v1/avatars ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/avatars")
  class CreateAvatar {

    @Test
    @DisplayName("returns 201 with the new avatar id")
    void shouldReturn201WithId() throws Exception {
      when(avatarService.createAvatar(new Id<>(AVATAR_ID), AVATAR_NAME))
          .thenReturn(new Id<>(AVATAR_ID));

      mockMvc
          .perform(
              post("/api/v1/avatars")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"id\":\"avatar-1\",\"name\": \"Hero\"}"))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(AVATAR_ID));
    }

    @Test
    @DisplayName("delegates name to the service")
    void shouldDelegateNameToService() throws Exception {
      when(avatarService.createAvatar(any(Id.class), anyString())).thenReturn(new Id<>(AVATAR_ID));

      mockMvc
          .perform(
              post("/api/v1/avatars")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"id\":\"avatar-1\",\"name\": \"Hero\"}"))
          .andExpect(status().isCreated());

      verify(avatarService).createAvatar(new Id<>(AVATAR_ID), "Hero");
    }
  }

  // ── GET /api/v1/avatars/{id} ─────────────────��────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/avatars/{id}")
  class GetAvatar {

    @Test
    @DisplayName("returns 200 with avatar data when found")
    void shouldReturn200WhenFound() throws Exception {
      when(avatarService.getAvatarById(new Id<>(AVATAR_ID))).thenReturn(stubAvatar());

      mockMvc
          .perform(get("/api/v1/avatars/{id}", AVATAR_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value(AVATAR_NAME));
    }

    @Test
    @DisplayName("returns 404 when avatar does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(avatarService.getAvatarById(new Id<>(UNKNOWN_ID)))
          .thenThrow(new AvatarNotFoundException(UNKNOWN_ID));

      mockMvc.perform(get("/api/v1/avatars/{id}", UNKNOWN_ID)).andExpect(status().isNotFound());
    }

    // ── GET /api/v1/avatars/search ────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/avatars/search")
    class SearchAvatars {

      @Test
      @DisplayName("returns 200 with matching avatars")
      void shouldReturn200WithResults() throws Exception {
        when(avatarService.searchAvatars(any())).thenReturn(List.of(stubAvatar()));

        mockMvc
            .perform(
                get("/api/v1/avatars/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {"name":"Hero","minLevel":null,"maxLevel":null}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._embedded.avatarResponseList[0].name").value(AVATAR_NAME));
      }

      @Test
      @DisplayName("returns 200 with empty list when no avatars match")
      void shouldReturn200WithEmptyList() throws Exception {
        when(avatarService.searchAvatars(any())).thenReturn(List.of());

        mockMvc
            .perform(
                get("/api/v1/avatars/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {"name":"unknown","minLevel":null,"maxLevel":null}
                    """))
            .andExpect(status().isOk());
      }

      @Test
      @DisplayName("delegates search criteria to the service")
      void shouldDelegateCriteriaToService() throws Exception {
        when(avatarService.searchAvatars(any())).thenReturn(List.of());

        mockMvc
            .perform(
                get("/api/v1/avatars/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {"name":"Hero","minLevel":1,"maxLevel":10}
                    """))
            .andExpect(status().isOk());

        verify(avatarService).searchAvatars(any());
      }

      @Test
      @DisplayName("returns 400 when domain rejects the search criteria")
      void shouldReturn400OnInvalidCriteria() throws Exception {
        when(avatarService.searchAvatars(any()))
            .thenThrow(new IllegalArgumentException("minLevel cannot be greater than maxLevel"));

        mockMvc
            .perform(
                get("/api/v1/avatars/search")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                    {"name":null,"minLevel":10,"maxLevel":1}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("minLevel cannot be greater than maxLevel"));
      }
    }
  }

  // ── DELETE /api/v1/avatars/{id} ───────────────────────────────────────────────

  @Nested
  @DisplayName("DELETE /api/v1/avatars/{id}")
  class DeleteAvatar {

    @Test
    @DisplayName("returns 204 on successful deletion")
    void shouldReturn204() throws Exception {
      doNothing().when(avatarService).deleteAvatar(new Id<>(AVATAR_ID));

      mockMvc.perform(delete("/api/v1/avatars/{id}", AVATAR_ID)).andExpect(status().isNoContent());

      verify(avatarService).deleteAvatar(new Id<>(AVATAR_ID));
    }

    @Test
    @DisplayName("returns 404 when avatar does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      doThrow(new AvatarNotFoundException(UNKNOWN_ID))
          .when(avatarService)
          .deleteAvatar(new Id<>(UNKNOWN_ID));

      mockMvc.perform(delete("/api/v1/avatars/{id}", UNKNOWN_ID)).andExpect(status().isNotFound());
    }
  }

  // ── PATCH /api/v1/avatars/{id}/name ──────────────────────────────────────────

  @Nested
  @DisplayName("PATCH /api/v1/avatars/{id}/name")
  class UpdateName {

    @Test
    @DisplayName("returns 204 and delegates new name to service")
    void shouldReturn204AndDelegate() throws Exception {
      doNothing().when(avatarService).updateName(eq(new Id<>(AVATAR_ID)), anyString());

      mockMvc
          .perform(
              patch("/api/v1/avatars/{id}/name", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\": \"NewHero\"}"))
          .andExpect(status().isNoContent());

      verify(avatarService).updateName(new Id<>(AVATAR_ID), "NewHero");
    }

    @Test
    @DisplayName("returns 400 when domain rejects blank name")
    void shouldReturn400OnBlankName() throws Exception {
      doThrow(new IllegalArgumentException("Name cannot be null or blank"))
          .when(avatarService)
          .updateName(eq(new Id<>(AVATAR_ID)), eq(""));

      mockMvc
          .perform(
              patch("/api/v1/avatars/{id}/name", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"name\": \"\"}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Name cannot be null or blank"));
    }
  }

  // ── GET /api/v1/avatars/{id}/money ────────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/avatars/{id}/money")
  class GetMoney {

    @Test
    @DisplayName("returns 200 with money amount")
    void shouldReturnMoney() throws Exception {
      when(avatarService.getMoney(new Id<>(AVATAR_ID))).thenReturn(new Money(250));

      mockMvc
          .perform(get("/api/v1/avatars/{id}/money", AVATAR_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.amount").value(250));
    }
  }

  // ── POST /api/v1/avatars/{id}/money/earn ─────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/avatars/{id}/money/earn")
  class EarnMoney {

    @Test
    @DisplayName("returns 204 and delegates to service")
    void shouldReturn204() throws Exception {
      doNothing().when(avatarService).earnMoney(new Id<>(AVATAR_ID), 100);

      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/money/earn", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"amount\": 100}"))
          .andExpect(status().isNoContent());

      verify(avatarService).earnMoney(new Id<>(AVATAR_ID), 100);
    }

    @Test
    @DisplayName("returns 400 when amount is not positive")
    void shouldReturn400OnNonPositiveAmount() throws Exception {
      doThrow(new IllegalArgumentException("Amount must be positive"))
          .when(avatarService)
          .earnMoney(new Id<>(AVATAR_ID), 0);

      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/money/earn", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"amount\": 0}"))
          .andExpect(status().isBadRequest());
    }
  }

  // ── POST /api/v1/avatars/{id}/money/spend ────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/avatars/{id}/money/spend")
  class SpendMoney {

    @Test
    @DisplayName("returns 204 on success")
    void shouldReturn204() throws Exception {
      doNothing().when(avatarService).spendMoney(new Id<>(AVATAR_ID), 50);

      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/money/spend", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"amount\": 50}"))
          .andExpect(status().isNoContent());

      verify(avatarService).spendMoney(new Id<>(AVATAR_ID), 50);
    }

    @Test
    @DisplayName("returns 400 when avatar has insufficient funds")
    void shouldReturn400OnInsufficientFunds() throws Exception {
      doThrow(new IllegalStateException("Not enough money"))
          .when(avatarService)
          .spendMoney(new Id<>(AVATAR_ID), 9999);

      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/money/spend", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"amount\": 9999}"))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Not enough money"));
    }
  }

  // ── GET /api/v1/avatars/{id}/level ────────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/avatars/{id}/level")
  class GetLevel {

    @Test
    @DisplayName("returns 200 with current level number")
    void shouldReturnLevel() throws Exception {
      Level level = new Level(3, new Experience(50), new Experience(200));
      when(avatarService.getLevel(new Id<>(AVATAR_ID))).thenReturn(level);

      mockMvc
          .perform(get("/api/v1/avatars/{id}/level", AVATAR_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.levelNumber").value(3))
          .andExpect(jsonPath("$.currentExperience").value(50))
          .andExpect(jsonPath("$.experienceRequired").value(200));
    }
  }

  // ── GET /api/v1/avatars/{id}/health ──────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/avatars/{id}/health")
  class GetHealth {

    @Test
    @DisplayName("returns 200 with current and max health")
    void shouldReturnHealth() throws Exception {
      AvatarHealth health = new AvatarHealth(new Health(75), new Health(100));
      when(avatarService.getHealth(new Id<>(AVATAR_ID))).thenReturn(health);

      mockMvc
          .perform(get("/api/v1/avatars/{id}/health", AVATAR_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.current").value(75))
          .andExpect(jsonPath("$.max").value(100));
    }
  }

  // ── POST /api/v1/avatars/{id}/health/damage ──────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/avatars/{id}/health/damage")
  class ApplyDamage {

    @Test
    @DisplayName("returns 204 and delegates damage amount")
    void shouldApplyDamage() throws Exception {
      when(avatarService.applyDamage(new Id<>(AVATAR_ID), 30)).thenReturn(false);

      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/health/damage", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"amount\": 30}"))
          .andExpect(status().is2xxSuccessful());

      verify(avatarService).applyDamage(new Id<>(AVATAR_ID), 30);
    }
  }

  // ── POST /api/v1/avatars/{id}/health/heal ────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/avatars/{id}/health/heal")
  class HealAvatar {

    @Test
    @DisplayName("returns 204 and delegates heal amount")
    void shouldHealAvatar() throws Exception {
      doNothing().when(avatarService).healAvatar(new Id<>(AVATAR_ID), 20);

      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/health/heal", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"amount\": 20}"))
          .andExpect(status().isNoContent());

      verify(avatarService).healAvatar(new Id<>(AVATAR_ID), 20);
    }
  }

  // ── GET /api/v1/avatars/{id}/mana ─────────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/avatars/{id}/mana")
  class GetMana {

    @Test
    @DisplayName("returns 200 with current and max mana")
    void shouldReturnMana() throws Exception {
      AvatarMana mana = new AvatarMana(new Mana(30), new Mana(50));
      when(avatarService.getMana(new Id<>(AVATAR_ID))).thenReturn(mana);

      mockMvc
          .perform(get("/api/v1/avatars/{id}/mana", AVATAR_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.amount").value(30))
          .andExpect(jsonPath("$.max").value(50));
    }
  }

  // ── POST /api/v1/avatars/{id}/mana/spend ─────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/avatars/{id}/mana/spend")
  class SpendMana {

    @Test
    @DisplayName("returns 204 on success")
    void shouldReturn204() throws Exception {
      doNothing().when(avatarService).spendMana(new Id<>(AVATAR_ID), 10);

      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/mana/spend", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"amount\": 10}"))
          .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("returns 400 when mana is insufficient")
    void shouldReturn400OnInsufficientMana() throws Exception {
      doThrow(new IllegalArgumentException("Cannot subtract more mana than available"))
          .when(avatarService)
          .spendMana(new Id<>(AVATAR_ID), 999);

      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/mana/spend", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"amount\": 999}"))
          .andExpect(status().isBadRequest());
    }
  }

  // ── POST /api/v1/avatars/{id}/mana/restore ───────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/avatars/{id}/mana/restore")
  class RestoreMana {

    @Test
    @DisplayName("returns 204 and delegates restore amount")
    void shouldRestoreMana() throws Exception {
      doNothing().when(avatarService).restoreMana(new Id<>(AVATAR_ID), 15);

      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/mana/restore", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"amount\": 15}"))
          .andExpect(status().isNoContent());

      verify(avatarService).restoreMana(new Id<>(AVATAR_ID), 15);
    }
  }

  // ── POST /api/v1/avatars/{id}/experience/grant ───────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/avatars/{id}/experience/grant")
  class GrantExperience {

    @Test
    @DisplayName("returns 204 and delegates XP amount")
    void shouldGrantExperience() throws Exception {
      doNothing().when(avatarService).grantExperience(new Id<>(AVATAR_ID), 500);

      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/experience/grant", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"amount\": 500}"))
          .andExpect(status().isNoContent());

      verify(avatarService).grantExperience(new Id<>(AVATAR_ID), 500);
    }
  }

  // ── GET /api/v1/avatars/{id}/experience ──────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/avatars/{id}/experience")
  class GetExperience {

    @Test
    @DisplayName("returns 200 with experience amount")
    void shouldReturnExperience() throws Exception {
      when(avatarService.getExperience(new Id<>(AVATAR_ID))).thenReturn(new Experience(350));

      mockMvc
          .perform(get("/api/v1/avatars/{id}/experience", AVATAR_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.amount").value(350));
    }

    @Test
    @DisplayName("returns 404 when avatar does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(avatarService.getExperience(new Id<>(UNKNOWN_ID)))
          .thenThrow(new AvatarNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/avatars/{id}/experience", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── POST /api/v1/avatars/{id}/inventory/items ────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/avatars/{id}/inventory/items")
  class AddItemToInventory {

    @Test
    @DisplayName("returns 204 and adds weapon to inventory")
    void shouldAddWeapon() throws Exception {
      doNothing().when(avatarService).addToInventory(eq(new Id<>(AVATAR_ID)), any(Item.class));

      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/inventory/items", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                  {"type":"WEAPON","name":"Iron Sword","description":"A basic sword","power":15}
                                  """))
          .andExpect(status().isNoContent());

      verify(avatarService).addToInventory(eq(new Id<>(AVATAR_ID)), any(Weapon.class));
    }

    @Test
    @DisplayName("returns 400 for unknown item type")
    void shouldReturn400ForUnknownItemType() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/inventory/items", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                  {"type":"UNKNOWN","name":"???","description":"???","power":0}
                                  """))
          .andExpect(status().isBadRequest());
    }
  }

  // ── DELETE /api/v1/avatars/{id}/inventory/items ──────────────────────────────

  @Nested
  @DisplayName("DELETE /api/v1/avatars/{id}/inventory/items")
  class RemoveItemFromInventory {

    @Test
    @DisplayName("returns 204 on successful removal")
    void shouldRemoveItem() throws Exception {
      doNothing().when(avatarService).removeItem(eq(new Id<>(AVATAR_ID)), any(Item.class));

      mockMvc
          .perform(
              delete("/api/v1/avatars/{id}/inventory/items", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                  {"type":"ARMOR","name":"Iron Shield","description":"A basic shield","power":5}
                                  """))
          .andExpect(status().isNoContent());
    }
  }

  // ── GET /api/v1/avatars/{id}/inventory ───────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/avatars/{id}/inventory")
  class GetInventory {

    @Test
    @DisplayName("returns 200 with inventory items")
    void shouldReturnInventory() throws Exception {
      Inventory inventory = new Inventory(new Id<>("inv-1"));
      inventory.addItem(new Weapon("Iron Sword", "A basic sword", 15));
      when(avatarService.getInventory(new Id<>(AVATAR_ID))).thenReturn(inventory);

      mockMvc.perform(get("/api/v1/avatars/{id}/inventory", AVATAR_ID)).andExpect(status().isOk());
    }
  }

  // ── GET /api/v1/avatars/{id}/equipped-items ──────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/avatars/{id}/equipped-items")
  class GetEquippedItems {

    @Test
    @DisplayName("returns 200 with equipped items list")
    void shouldReturnEquippedItems() throws Exception {
      EquippedItems equippedItems = new EquippedItems(new Id<>("equip-1"));
      when(avatarService.getEquippedItems(new Id<>(AVATAR_ID))).thenReturn(equippedItems);

      mockMvc
          .perform(get("/api/v1/avatars/{id}/equipped-items", AVATAR_ID))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when avatar does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(avatarService.getEquippedItems(new Id<>(UNKNOWN_ID)))
          .thenThrow(new AvatarNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/avatars/{id}/equipped-items", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── POST /api/v1/avatars/{id}/inventory/items/equip ──────────────────────────

  @Nested
  @DisplayName("POST /api/v1/avatars/{id}/inventory/items/equip")
  class EquipItem {

    @Test
    @DisplayName("returns 204 and delegates to service")
    void shouldEquipItem() throws Exception {
      doNothing().when(avatarService).equipItem(eq(new Id<>(AVATAR_ID)), any(Item.class));

      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/inventory/items/equip", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                  {"type":"WEAPON","name":"Iron Sword","description":"A basic sword","power":15}
                                  """))
          .andExpect(status().isNoContent());

      verify(avatarService).equipItem(eq(new Id<>(AVATAR_ID)), any(Item.class));
    }

    @Test
    @DisplayName("returns 400 when item is not in inventory")
    void shouldReturn400WhenItemNotInInventory() throws Exception {
      doThrow(new IllegalStateException("Item not in inventory"))
          .when(avatarService)
          .equipItem(eq(new Id<>(AVATAR_ID)), any(Item.class));

      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/inventory/items/equip", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                  {"type":"WEAPON","name":"Iron Sword","description":"A basic sword","power":15}
                                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.message").value("Item not in inventory"));
    }
  }

  // ── POST /api/v1/avatars/{id}/inventory/items/unequip ────────────────────────

  @Nested
  @DisplayName("POST /api/v1/avatars/{id}/inventory/items/unequip")
  class UnequipItem {

    @Test
    @DisplayName("returns 204 and delegates to service")
    void shouldUnequipItem() throws Exception {
      doNothing().when(avatarService).unequipItem(eq(new Id<>(AVATAR_ID)), any(Item.class));

      mockMvc
          .perform(
              post("/api/v1/avatars/{id}/inventory/items/unequip", AVATAR_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                  {"type":"ARMOR","name":"Iron Shield","description":"A basic shield","power":5}
                                  """))
          .andExpect(status().isNoContent());

      verify(avatarService).unequipItem(eq(new Id<>(AVATAR_ID)), any(Item.class));
    }
  }

  // ── POST /api/v1/avatars/{id}/stats/strength ─────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/avatars/{id}/stats/strength")
  class IncreaseStrength {

    @Test
    @DisplayName("returns 204 and delegates to service")
    void shouldIncreaseStrength() throws Exception {
      doNothing().when(avatarService).increaseStrength(new Id<>(AVATAR_ID));

      mockMvc
          .perform(post("/api/v1/avatars/{id}/stats/strength", AVATAR_ID))
          .andExpect(status().isNoContent());

      verify(avatarService).increaseStrength(new Id<>(AVATAR_ID));
    }

    @Test
    @DisplayName("returns 404 when avatar not found")
    void shouldReturn404WhenAvatarMissing() throws Exception {
      doThrow(new AvatarNotFoundException(UNKNOWN_ID))
          .when(avatarService)
          .increaseStrength(new Id<>(UNKNOWN_ID));

      mockMvc
          .perform(post("/api/v1/avatars/{id}/stats/strength", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── POST /api/v1/avatars/{id}/stats/defense ──────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/avatars/{id}/stats/defense")
  class IncreaseDefense {

    @Test
    @DisplayName("returns 204 and delegates to service")
    void shouldIncreaseDefense() throws Exception {
      doNothing().when(avatarService).increaseDefense(new Id<>(AVATAR_ID));

      mockMvc
          .perform(post("/api/v1/avatars/{id}/stats/defense", AVATAR_ID))
          .andExpect(status().isNoContent());

      verify(avatarService).increaseDefense(new Id<>(AVATAR_ID));
    }

    @Test
    @DisplayName("returns 404 when avatar not found")
    void shouldReturn404WhenAvatarMissing() throws Exception {
      doThrow(new AvatarNotFoundException(UNKNOWN_ID))
          .when(avatarService)
          .increaseDefense(new Id<>(UNKNOWN_ID));

      mockMvc
          .perform(post("/api/v1/avatars/{id}/stats/defense", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── POST /api/v1/avatars/{id}/stats/intelligence ─────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/avatars/{id}/stats/intelligence")
  class IncreaseIntelligence {

    @Test
    @DisplayName("returns 204 and delegates to service")
    void shouldIncreaseIntelligence() throws Exception {
      doNothing().when(avatarService).increaseIntelligence(new Id<>(AVATAR_ID));

      mockMvc
          .perform(post("/api/v1/avatars/{id}/stats/intelligence", AVATAR_ID))
          .andExpect(status().isNoContent());

      verify(avatarService).increaseIntelligence(new Id<>(AVATAR_ID));
    }

    @Test
    @DisplayName("returns 404 when avatar not found")
    void shouldReturn404WhenAvatarMissing() throws Exception {
      doThrow(new AvatarNotFoundException(UNKNOWN_ID))
          .when(avatarService)
          .increaseIntelligence(new Id<>(UNKNOWN_ID));

      mockMvc
          .perform(post("/api/v1/avatars/{id}/stats/intelligence", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/avatars/{id}/stats ───────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/avatars/{id}/stats")
  class GetStats {

    @Test
    @DisplayName("returns 200 with avatar stats")
    void shouldReturnStats() throws Exception {
      AvatarStats stats = new AvatarStats(new Id<>("stats-1"), 10, 8, 12);
      when(avatarService.getAvatarStats(new Id<>(AVATAR_ID))).thenReturn(stats);

      mockMvc
          .perform(get("/api/v1/avatars/{id}/stats", AVATAR_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.strength").value(10))
          .andExpect(jsonPath("$.defense").value(8))
          .andExpect(jsonPath("$.intelligence").value(12));
    }

    @Test
    @DisplayName("returns 404 when avatar does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(avatarService.getAvatarStats(new Id<>(UNKNOWN_ID)))
          .thenThrow(new AvatarNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/avatars/{id}/stats", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }
}
