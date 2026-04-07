package habitquest.guild.infrastructure;

import static habitquest.guild.GuildFixtures.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.ddd.Id;
import habitquest.guild.application.*;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.battle.boss.BossType;
import habitquest.guild.domain.battle.stats.Health;
import java.util.List;
import java.util.Optional;
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
  @MockitoBean private GuildLogger log;

  // ── POST /api/v1/battles ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/battles")
  class CreateBattle {

    @Test
    @DisplayName("returns 201 with the new battle id")
    void shouldReturn201WithId() throws Exception {
      when(guildService.isLeader(GUILD_ID, LEADER_HTTP)).thenReturn(true);
      when(guildService.getMembers(GUILD_ID)).thenReturn(List.of(avatarMember()));
      when(battleService.createBattle(eq(GUILD_ID), eq(BossType.MINOTAUR), eq(1), any()))
          .thenReturn(BATTLE_ID);

      mockMvc
          .perform(
              post("/api/v1/battles")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","bossType":"MINOTAUR","requesterId":"leader-1"}
                                      """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(BATTLE_ID.value()));
    }

    @Test
    @DisplayName("delegates guild, boss and turn count (derived from member count) to the service")
    void shouldDelegateToService() throws Exception {
      when(guildService.isLeader(any(Id.class), any(Id.class))).thenReturn(true);
      when(guildService.getMembers(any(Id.class)))
          .thenReturn(List.of(avatarMember(), avatarMember()));
      when(battleService.createBattle(any(Id.class), any(BossType.class), anyInt(), any()))
          .thenReturn(BATTLE_ID);

      mockMvc
          .perform(
              post("/api/v1/battles")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","bossType":"MINOTAUR","requesterId":"leader-1"}
                                      """))
          .andExpect(status().isCreated());

      verify(battleService).createBattle(eq(GUILD_ID), eq(BossType.MINOTAUR), eq(2), any());
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
                                      {"guildId":"guild-1","bossType":"DRAGON","requesterId":"leader-1"}
                                      """))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(battleService);
    }

    @Test
    @DisplayName("returns 403 when requestor is not the guild leader")
    void shouldReturn403WhenNotLeader() throws Exception {
      Id<habitquest.guild.domain.guild.GuildMember> notLeader = new Id<>("not-a-leader");
      when(guildService.isLeader(eq(GUILD_ID), eq(notLeader))).thenReturn(false);

      mockMvc
          .perform(
              post("/api/v1/battles")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","bossType":"MINOTAUR","requesterId":"not-a-leader"}
                                      """))
          .andExpect(status().isForbidden());

      verifyNoInteractions(battleService);
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenGuildNotFound() throws Exception {
      when(guildService.isLeader(eq(UNKNOWN_GUILD_ID), eq(LEADER_HTTP)))
          .thenThrow(new GuildNotFoundException(UNKNOWN_GUILD_ID.value()));

      mockMvc
          .perform(
              post("/api/v1/battles")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"ghost-99","bossType":"MINOTAUR","requesterId":"leader-1"}
                                      """))
          .andExpect(status().isNotFound());

      verifyNoInteractions(battleService);
    }
  }

  // ── GET /api/v1/battles/{id} ──────────────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/battles/{id}")
  class GetBattle {

    @Test
    @DisplayName("returns 200 with battle data when found")
    void shouldReturn200WhenFound() throws Exception {
      when(battleService.getBattleById(BATTLE_ID)).thenReturn(battle());

      mockMvc.perform(get("/api/v1/battles/{id}", BATTLE_ID.value())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBattleById(UNKNOWN_BATTLE_ID))
          .thenThrow(new BattleNotFoundException(UNKNOWN_BATTLE_ID.value()));

      mockMvc
          .perform(get("/api/v1/battles/{id}", UNKNOWN_BATTLE_ID.value()))
          .andExpect(status().isNotFound());
    }
  }

  // ── DELETE /api/v1/battles/{id} ───────────────────────────────────────────────

  @Nested
  @DisplayName("DELETE /api/v1/battles/{id}")
  class DeleteBattle {

    @Test
    @DisplayName("returns 204 when leader deletes a battle")
    void shouldReturn204() throws Exception {
      when(guildService.isLeader(GUILD_ID, LEADER_HTTP)).thenReturn(true);
      doNothing().when(battleService).deleteBattle(BATTLE_ID);

      mockMvc
          .perform(
              delete("/api/v1/battles/{id}", BATTLE_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","requesterId":"leader-1"}
                                      """))
          .andExpect(status().isNoContent());

      verify(battleService).deleteBattle(BATTLE_ID);
    }

    @Test
    @DisplayName("returns 403 when requestor is not the guild leader")
    void shouldReturn403WhenNotLeader() throws Exception {
      Id<habitquest.guild.domain.guild.GuildMember> notLeader = new Id<>("not-a-leader");
      when(guildService.isLeader(eq(GUILD_ID), eq(notLeader))).thenReturn(false);

      mockMvc
          .perform(
              delete("/api/v1/battles/{id}", BATTLE_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","requesterId":"not-a-leader"}
                                      """))
          .andExpect(status().isForbidden());

      verify(battleService, never()).deleteBattle(any());
    }

    @Test
    @DisplayName("returns 204 even when battle does not exist (idempotent delete)")
    void shouldReturn204WhenNotFound() throws Exception {
      when(guildService.isLeader(GUILD_ID, LEADER_HTTP)).thenReturn(true);

      mockMvc
          .perform(
              delete("/api/v1/battles/{id}", UNKNOWN_BATTLE_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","requesterId":"leader-1"}
                                      """))
          .andExpect(status().isNoContent());
    }
  }

  // ── GET /api/v1/battles/guild/{guildId} ───────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/battles/guild/{guildId}")
  class GetBattleByGuild {

    @Test
    @DisplayName("returns 200 with the active battle for the guild")
    void shouldReturn200() throws Exception {
      when(battleService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.of(battle()));

      mockMvc
          .perform(get("/api/v1/battles/guild/{guildId}", GUILD_ID.value()))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when no battle exists for the guild")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBattleByGuild(UNKNOWN_GUILD_ID))
          .thenThrow(new BattleNotFoundException(UNKNOWN_GUILD_ID.value()));

      mockMvc
          .perform(get("/api/v1/battles/guild/{guildId}", UNKNOWN_GUILD_ID.value()))
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
          .perform(get("/api/v1/battles/guild/{guildId}/in-progress", GUILD_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.inProgress").value(true));
    }

    @Test
    @DisplayName("returns 200 with inProgress=false when no battle is running")
    void shouldReturnFalseWhenNotInProgress() throws Exception {
      when(battleService.hasBattleInProgress(GUILD_ID)).thenReturn(false);

      mockMvc
          .perform(get("/api/v1/battles/guild/{guildId}/in-progress", GUILD_ID.value()))
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
      when(battleService.getBoss(BATTLE_ID)).thenReturn(BossType.MINOTAUR);

      mockMvc
          .perform(get("/api/v1/battles/{id}/boss", BATTLE_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("MINOTAUR"));
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBoss(UNKNOWN_BATTLE_ID))
          .thenThrow(new BattleNotFoundException(UNKNOWN_BATTLE_ID.value()));

      mockMvc
          .perform(get("/api/v1/battles/{id}/boss", UNKNOWN_BATTLE_ID.value()))
          .andExpect(status().isNotFound());
    }
  }

  // ── GET /api/v1/battles/{id}/boss/health ─────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/battles/{id}/boss/health")
  class GetBossHealth {

    @Test
    @DisplayName("returns 200 with remaining health value")
    void shouldReturn200() throws Exception {
      when(battleService.getBossRemainingHealth(BATTLE_ID))
          .thenReturn(new BossStatus(new Health(800)));

      mockMvc
          .perform(get("/api/v1/battles/{id}/boss/health", BATTLE_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.remainingHealth").value(800));
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBossRemainingHealth(UNKNOWN_BATTLE_ID))
          .thenThrow(new BattleNotFoundException(UNKNOWN_BATTLE_ID.value()));

      mockMvc
          .perform(get("/api/v1/battles/{id}/boss/health", UNKNOWN_BATTLE_ID.value()))
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
          .perform(get("/api/v1/battles/{id}/turns/current", BATTLE_ID.value()))
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
          .perform(get("/api/v1/battles/{id}/turns/total", BATTLE_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.turn").value(5));
    }
  }

  // ── POST /api/v1/battles/{id}/damage ─────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/battles/{id}/damage")
  class DealDamage {

    private void stubAttackerTurn() throws BattleNotFoundException {
      when(battleService.isAttackerTurn(BATTLE_ID, LEADER_ID)).thenReturn(true);
    }

    @Test
    @DisplayName("grants XP and money to all members when boss is killed (Won)")
    void shouldGrantRewardsOnWin() throws Exception {
      stubAttackerTurn();
      when(battleService.dealDamageOnBoss(BATTLE_ID, LEADER_ID, 500))
          .thenReturn(new BattleOutcome.Won(EXP_REWARD, MONEY_REWARD));
      when(battleService.getBattleById(BATTLE_ID)).thenReturn(battle());

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", BATTLE_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":500,"attackerAvatarId":"avatar-1"}
                                      """))
          .andExpect(status().isNoContent());

      verify(avatarClient).grantExperience(eq(LEADER_ID.value()), eq(EXP_REWARD));
      verify(avatarClient).earnMoney(eq(LEADER_ID.value()), eq(MONEY_REWARD));
      verifyNoMoreInteractions(avatarClient);
    }

    @Test
    @DisplayName("applies penalty to all members when counterattack triggers Lost")
    void shouldApplyPenaltyOnLoss() throws Exception {
      stubAttackerTurn();
      when(battleService.dealDamageOnBoss(BATTLE_ID, LEADER_ID, 10))
          .thenReturn(new BattleOutcome.Ongoing());
      when(avatarClient.applyDamage(LEADER_ID.value(), 10))
          .thenReturn(new AvatarClient.DamageResult(true));
      when(battleService.applyCounterattack(BATTLE_ID, LEADER_ID))
          .thenReturn(new BattleOutcome.Lost(PENALTY));
      when(battleService.getBattleById(BATTLE_ID)).thenReturn(battle());

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", BATTLE_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":10,"attackerAvatarId":"avatar-1"}
                                      """))
          .andExpect(status().isNoContent());

      verify(avatarClient).applyPenalty(eq(LEADER_ID.value()), eq(PENALTY));
    }

    @Test
    @DisplayName(
        "advances turn and applies damage to attacker when boss survives and attacker lives")
    void shouldAdvanceTurnWhenOngoingAndAttackerLives() throws Exception {
      stubAttackerTurn();
      when(battleService.dealDamageOnBoss(BATTLE_ID, LEADER_ID, 30))
          .thenReturn(new BattleOutcome.Ongoing());
      when(avatarClient.applyDamage(LEADER_ID.value(), 30))
          .thenReturn(new AvatarClient.DamageResult(false));

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", BATTLE_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":30,"attackerAvatarId":"avatar-1"}
                                      """))
          .andExpect(status().isNoContent());

      verify(avatarClient).applyDamage(LEADER_ID.value(), 30);
      verify(battleService).nextTurn(BATTLE_ID);
      verify(battleService, never()).applyCounterattack(any(), any());
    }

    @Test
    @DisplayName("applies counterattack and advances turn when attacker dies but guild survives")
    void shouldApplyCounterattackAndAdvanceTurnWhenAttackerDiesButGuildSurvives() throws Exception {
      stubAttackerTurn();
      when(battleService.dealDamageOnBoss(BATTLE_ID, LEADER_ID, 30))
          .thenReturn(new BattleOutcome.Ongoing());
      when(avatarClient.applyDamage(LEADER_ID.value(), 30))
          .thenReturn(new AvatarClient.DamageResult(true));
      when(battleService.applyCounterattack(BATTLE_ID, LEADER_ID))
          .thenReturn(new BattleOutcome.Ongoing());

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", BATTLE_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":30,"attackerAvatarId":"avatar-1"}
                                      """))
          .andExpect(status().isNoContent());

      verify(battleService).applyCounterattack(BATTLE_ID, LEADER_ID);
      verify(battleService).nextTurn(BATTLE_ID);
    }

    @Test
    @DisplayName("returns 403 when it is not the attacker's turn")
    void shouldReturn403WhenWrongAttacker() throws Exception {
      Id<habitquest.guild.domain.guild.GuildMember> wrongAvatar = new Id<>("wrong-avatar");
      when(battleService.isAttackerTurn(eq(BATTLE_ID), eq(wrongAvatar))).thenReturn(false);

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", BATTLE_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":30,"attackerAvatarId":"wrong-avatar"}
                                      """))
          .andExpect(status().isForbidden());

      verifyNoInteractions(avatarClient);
    }

    @Test
    @DisplayName("returns 400 when attackerAvatarId is null")
    void shouldReturn400WhenNoAttackerProvided() throws Exception {
      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", BATTLE_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":30,"attackerAvatarId":null}
                                      """))
          .andExpect(status().isBadRequest());

      verifyNoInteractions(battleService);
      verifyNoInteractions(avatarClient);
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenBattleNotFound() throws Exception {
      when(battleService.isAttackerTurn(eq(UNKNOWN_BATTLE_ID), eq(LEADER_ID)))
          .thenThrow(new BattleNotFoundException(UNKNOWN_BATTLE_ID.value()));

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", UNKNOWN_BATTLE_ID.value())
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
    @DisplayName("returns 200 with isOver=false and isWon=false when battle is Ongoing")
    void shouldReturnOngoingStatus() throws Exception {
      when(battleService.getBattleStatus(BATTLE_ID)).thenReturn(new BattleOutcome.Ongoing());
      when(battleService.isBattleOver(BATTLE_ID)).thenReturn(false);
      when(battleService.isBattleWon(BATTLE_ID)).thenReturn(false);

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", BATTLE_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isOver").value(false))
          .andExpect(jsonPath("$.isWon").value(false));
    }

    @Test
    @DisplayName("returns 200 with isOver=true and isWon=true when battle is Won")
    void shouldReturnWonStatus() throws Exception {
      when(battleService.getBattleStatus(BATTLE_ID))
          .thenReturn(new BattleOutcome.Won(EXP_REWARD, MONEY_REWARD));
      when(battleService.isBattleOver(BATTLE_ID)).thenReturn(true);
      when(battleService.isBattleWon(BATTLE_ID)).thenReturn(true);

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", BATTLE_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isOver").value(true))
          .andExpect(jsonPath("$.isWon").value(true));
    }

    @Test
    @DisplayName("returns 200 with isOver=true and isWon=false when battle is Lost")
    void shouldReturnLostStatus() throws Exception {
      when(battleService.getBattleStatus(BATTLE_ID)).thenReturn(new BattleOutcome.Lost(PENALTY));
      when(battleService.isBattleOver(BATTLE_ID)).thenReturn(true);
      when(battleService.isBattleWon(BATTLE_ID)).thenReturn(false);

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", BATTLE_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isOver").value(true))
          .andExpect(jsonPath("$.isWon").value(false));
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBattleStatus(UNKNOWN_BATTLE_ID))
          .thenThrow(new BattleNotFoundException(UNKNOWN_BATTLE_ID.value()));

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", UNKNOWN_BATTLE_ID.value()))
          .andExpect(status().isNotFound());
    }
  }
}
