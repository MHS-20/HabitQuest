package habitquest.guild.infrastructure;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import habitquest.guild.application.BattleNotFoundException;
import habitquest.guild.application.BattleService;
import habitquest.guild.application.GuildService;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleStatus;
import habitquest.guild.domain.battle.boss.BossEnemy;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.battle.boss.Minotaur;
import habitquest.guild.domain.battle.stats.Health;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
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

@WebMvcTest(BattleController.class)
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@DisplayName("BattleController")
public class BattleControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private BattleService battleService;
  @MockitoBean private GuildService guildService;
  @MockitoBean private AvatarClient avatarClient;

  // ── Fixtures ──────────────────────────────────────────────────────────────────

  private static final String BATTLE_ID = "battle-1";
  private static final String GUILD_ID = "guild-1";
  private static final String AVATAR_ID = "avatar-1";
  private static final String UNKNOWN_ID = "ghost-99";

  private Battle stubBattle() {
    return new Battle(
        BATTLE_ID, GUILD_ID, Minotaur.INSTANCE, 10, 0, new BossStatus(new Health(1000)));
  }

  private GuildMember stubMember() {
    return new GuildMember(AVATAR_ID, "Hero", new GuildRole("MEMBER"));
  }

  // ── POST /api/v1/battles ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/battles")
  class CreateBattle {

    @Test
    @DisplayName("returns 201 with the new battle id")
    void shouldReturn201WithId() throws Exception {
      when(battleService.createBattle(eq(GUILD_ID), any(BossEnemy.class), eq(5)))
          .thenReturn(BATTLE_ID);

      mockMvc
          .perform(
              post("/api/v1/battles")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","bossType":"MINOTAUR","numOfTurns":5}
                                      """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(BATTLE_ID));
    }

    @Test
    @DisplayName("delegates guild, boss and turn count to the service")
    void shouldDelegateToService() throws Exception {
      when(battleService.createBattle(anyString(), any(BossEnemy.class), anyInt()))
          .thenReturn(BATTLE_ID);

      mockMvc
          .perform(
              post("/api/v1/battles")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","bossType":"MINOTAUR","numOfTurns":5}
                                      """))
          .andExpect(status().isCreated());

      verify(battleService).createBattle(eq("guild-1"), any(BossEnemy.class), eq(5));
    }

    @Test
    @DisplayName("returns 400 for an unknown boss type")
    void shouldReturn400ForUnknownBossType() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/battles")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","bossType":"DRAGON","numOfTurns":5}
                                      """))
          .andExpect(status().isBadRequest());
    }
  }

  // ── GET /api/v1/battles/{id} ──────────────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/battles/{id}")
  class GetBattle {

    @Test
    @DisplayName("returns 200 with battle data when found")
    void shouldReturn200WhenFound() throws Exception {
      when(battleService.getBattleById(BATTLE_ID)).thenReturn(stubBattle());

      mockMvc.perform(get("/api/v1/battles/{id}", BATTLE_ID)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBattleById(UNKNOWN_ID))
          .thenThrow(new BattleNotFoundException(UNKNOWN_ID));

      mockMvc.perform(get("/api/v1/battles/{id}", UNKNOWN_ID)).andExpect(status().isNotFound());
    }
  }

  // ── DELETE /api/v1/battles/{id} ───────────────────────────────────────────────

  @Nested
  @DisplayName("DELETE /api/v1/battles/{id}")
  class DeleteBattle {

    @Test
    @DisplayName("returns 204 on successful deletion")
    void shouldReturn204() throws Exception {
      doNothing().when(battleService).deleteBattle(BATTLE_ID);

      mockMvc.perform(delete("/api/v1/battles/{id}", BATTLE_ID)).andExpect(status().isNoContent());

      verify(battleService).deleteBattle(BATTLE_ID);
    }
  }

  // ── GET /api/v1/battles/guild/{guildId} ───────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/battles/guild/{guildId}")
  class GetBattleByGuild {

    @Test
    @DisplayName("returns 200 with the active battle for the guild")
    void shouldReturn200() throws Exception {
      when(battleService.getBattleByGuild(GUILD_ID)).thenReturn(stubBattle());

      mockMvc.perform(get("/api/v1/battles/guild/{guildId}", GUILD_ID)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when no battle exists for the guild")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBattleByGuild(UNKNOWN_ID))
          .thenThrow(new BattleNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/battles/guild/{guildId}", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/battles/guild/{guildId}/in-progress ──────────────────────────

  @Nested
  @DisplayName("GET /api/v1/battles/guild/{guildId}/in-progress")
  class HasBattleInProgress {

    @Test
    @DisplayName("returns 200 with inProgress=true when a battle is running")
    void shouldReturnTrueWhenInProgress() throws Exception {
      when(battleService.hasBattleInProgress(GUILD_ID)).thenReturn(true);

      mockMvc
          .perform(get("/api/v1/battles/guild/{guildId}/in-progress", GUILD_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.inProgress").value(true));
    }

    @Test
    @DisplayName("returns 200 with inProgress=false when no battle is running")
    void shouldReturnFalseWhenNotInProgress() throws Exception {
      when(battleService.hasBattleInProgress(GUILD_ID)).thenReturn(false);

      mockMvc
          .perform(get("/api/v1/battles/guild/{guildId}/in-progress", GUILD_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.inProgress").value(false));
    }
  }

  // ── GET /api/v1/battles/{id}/boss ────────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/battles/{id}/boss")
  class GetBoss {

    @Test
    @DisplayName("returns 200 with boss data")
    void shouldReturn200() throws Exception {
      when(battleService.getBoss(BATTLE_ID)).thenReturn(Minotaur.INSTANCE);

      mockMvc.perform(get("/api/v1/battles/{id}/boss", BATTLE_ID)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBoss(UNKNOWN_ID)).thenThrow(new BattleNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/battles/{id}/boss", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/battles/{id}/boss/health ─────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/battles/{id}/boss/health")
  class GetBossHealth {

    @Test
    @DisplayName("returns 200 with boss status")
    void shouldReturn200() throws Exception {
      when(battleService.getBossRemainingHealth(BATTLE_ID))
          .thenReturn(new BossStatus(new Health(800)));

      mockMvc
          .perform(get("/api/v1/battles/{id}/boss/health", BATTLE_ID))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBossRemainingHealth(UNKNOWN_ID))
          .thenThrow(new BattleNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/battles/{id}/boss/health", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/battles/{id}/turns/current ───────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/battles/{id}/turns/current")
  class GetCurrentTurn {

    @Test
    @DisplayName("returns 200 with current turn number")
    void shouldReturnCurrentTurn() throws Exception {
      when(battleService.getCurrentTurn(BATTLE_ID)).thenReturn(2);

      mockMvc
          .perform(get("/api/v1/battles/{id}/turns/current", BATTLE_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.turn").value(2));
    }
  }

  // ── GET /api/v1/battles/{id}/turns/total ─────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/battles/{id}/turns/total")
  class GetNumOfTurns {

    @Test
    @DisplayName("returns 200 with total turn count")
    void shouldReturnTotalTurns() throws Exception {
      when(battleService.getNumOfTurns(BATTLE_ID)).thenReturn(5);

      mockMvc
          .perform(get("/api/v1/battles/{id}/turns/total", BATTLE_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.turn").value(5));
    }
  }

  // ── POST /api/v1/battles/{id}/turns/next ─────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/battles/{id}/turns/next")
  class NextTurn {

    @Test
    @DisplayName("returns 204 and advances the turn")
    void shouldReturn204AndAdvanceTurn() throws Exception {
      doNothing().when(battleService).nextTurn(BATTLE_ID);

      mockMvc
          .perform(post("/api/v1/battles/{id}/turns/next", BATTLE_ID))
          .andExpect(status().isNoContent());

      verify(battleService).nextTurn(BATTLE_ID);
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      doThrow(new BattleNotFoundException(UNKNOWN_ID)).when(battleService).nextTurn(UNKNOWN_ID);

      mockMvc
          .perform(post("/api/v1/battles/{id}/turns/next", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }

  // ── POST /api/v1/battles/{id}/turns/increase ─────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/battles/{id}/turns/increase")
  class IncreaseNumOfTurns {

    @Test
    @DisplayName("returns 204 and increases total turns")
    void shouldReturn204AndIncrease() throws Exception {
      doNothing().when(battleService).increaseNumOfTurn(BATTLE_ID);

      mockMvc
          .perform(post("/api/v1/battles/{id}/turns/increase", BATTLE_ID))
          .andExpect(status().isNoContent());

      verify(battleService).increaseNumOfTurn(BATTLE_ID);
    }
  }

  // ── POST /api/v1/battles/{id}/damage ─────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/battles/{id}/damage")
  class DealDamage {

    @Test
    @DisplayName("grants XP and money to all members when battle is WON")
    void shouldGrantRewardsOnWin() throws Exception {
      when(battleService.getBoss(BATTLE_ID)).thenReturn(Minotaur.INSTANCE);
      when(battleService.getGuildId(BATTLE_ID)).thenReturn(GUILD_ID);
      when(guildService.getMembers(GUILD_ID)).thenReturn(List.of(stubMember()));
      when(battleService.getBattleStatus(BATTLE_ID)).thenReturn(BattleStatus.WON);

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", BATTLE_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":500,"attackerAvatarId":"avatar-1"}
                                      """))
          .andExpect(status().isNoContent());

      verify(avatarClient).grantExperience(eq(AVATAR_ID), anyInt());
      verify(avatarClient).earnMoney(eq(AVATAR_ID), anyInt());
    }

    @Test
    @DisplayName("applies penalty to all members when battle is LOST")
    void shouldApplyPenaltyOnLoss() throws Exception {
      when(battleService.getBoss(BATTLE_ID)).thenReturn(Minotaur.INSTANCE);
      when(battleService.getGuildId(BATTLE_ID)).thenReturn(GUILD_ID);
      when(guildService.getMembers(GUILD_ID)).thenReturn(List.of(stubMember()));
      when(battleService.getBattleStatus(BATTLE_ID)).thenReturn(BattleStatus.LOST);

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", BATTLE_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":0,"attackerAvatarId":null}
                                      """))
          .andExpect(status().isNoContent());

      verify(avatarClient).applyPenalty(eq(AVATAR_ID), anyInt());
      verifyNoMoreInteractions(avatarClient);
    }

    @Test
    @DisplayName("applies damage to the attacker when battle is ONGOING")
    void shouldApplyDamageToAttackerWhenOngoing() throws Exception {
      when(battleService.getBoss(BATTLE_ID)).thenReturn(Minotaur.INSTANCE);
      when(battleService.getGuildId(BATTLE_ID)).thenReturn(GUILD_ID);
      when(guildService.getMembers(GUILD_ID)).thenReturn(List.of(stubMember()));
      when(battleService.getBattleStatus(BATTLE_ID)).thenReturn(BattleStatus.ONGOING);

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", BATTLE_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":30,"attackerAvatarId":"avatar-1"}
                                      """))
          .andExpect(status().isNoContent());

      verify(avatarClient).applyDamage(AVATAR_ID, 30);
    }

    @Test
    @DisplayName("skips avatar damage call when no attacker is provided during ONGOING battle")
    void shouldSkipAvatarDamageWhenNoAttackerOnOngoing() throws Exception {
      when(battleService.getBoss(BATTLE_ID)).thenReturn(Minotaur.INSTANCE);
      when(battleService.getGuildId(BATTLE_ID)).thenReturn(GUILD_ID);
      when(guildService.getMembers(GUILD_ID)).thenReturn(List.of(stubMember()));
      when(battleService.getBattleStatus(BATTLE_ID)).thenReturn(BattleStatus.ONGOING);

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", BATTLE_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":30,"attackerAvatarId":null}
                                      """))
          .andExpect(status().isNoContent());

      verifyNoInteractions(avatarClient);
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenBattleNotFound() throws Exception {
      when(battleService.getBoss(UNKNOWN_ID)).thenThrow(new BattleNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", UNKNOWN_ID)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":10,"attackerAvatarId":"avatar-1"}
                                      """))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/battles/{id}/status ──────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/battles/{id}/status")
  class GetBattleStatus {

    @Test
    @DisplayName("returns 200 with full status when battle is ONGOING")
    void shouldReturnOngoingStatus() throws Exception {
      when(battleService.getBattleStatus(BATTLE_ID)).thenReturn(BattleStatus.ONGOING);
      when(battleService.isBattleOver(BATTLE_ID)).thenReturn(false);
      when(battleService.isBattleWon(BATTLE_ID)).thenReturn(false);

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", BATTLE_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("ONGOING"))
          .andExpect(jsonPath("$.isOver").value(false))
          .andExpect(jsonPath("$.isWon").value(false));
    }

    @Test
    @DisplayName("returns 200 with isOver=true and isWon=true when battle is WON")
    void shouldReturnWonStatus() throws Exception {
      when(battleService.getBattleStatus(BATTLE_ID)).thenReturn(BattleStatus.WON);
      when(battleService.isBattleOver(BATTLE_ID)).thenReturn(true);
      when(battleService.isBattleWon(BATTLE_ID)).thenReturn(true);

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", BATTLE_ID))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.status").value("WON"))
          .andExpect(jsonPath("$.isOver").value(true))
          .andExpect(jsonPath("$.isWon").value(true));
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBattleStatus(UNKNOWN_ID))
          .thenThrow(new BattleNotFoundException(UNKNOWN_ID));

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", UNKNOWN_ID))
          .andExpect(status().isNotFound());
    }
  }
}
