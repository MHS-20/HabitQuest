package habitquest.guild.infrastructure;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.ddd.Id;
import habitquest.guild.application.BattleNotFoundException;
import habitquest.guild.application.BattleService;
import habitquest.guild.application.GuildNotFoundException;
import habitquest.guild.application.GuildService;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.battle.boss.BossType;
import habitquest.guild.domain.battle.stats.Health;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import habitquest.guild.domain.guild.GuildRole;
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

  // ── Fixtures ──────────────────────────────────────────────────────────────────

  // Typed ids — used for Mockito when()/verify() calls only
  private static final Id<Battle> ID_BATTLE = new Id<>("battle-1");
  private static final Id<Guild> ID_GUILD = new Id<>("guild-1");
  private static final Id<GuildMember> ID_AVATAR = new Id<>("avatar-1");
  private static final Id<GuildMember> ID_LEADER = new Id<>("leader-1");
  private static final Id<Guild> ID_UNKNOWN_GUILD = new Id<>("ghost-99");
  private static final Id<Battle> ID_UNKNOWN_BATTLE = new Id<>("ghost-99");

  private static final int EXP_REWARD = BossType.MINOTAUR.experienceReward().amount();
  private static final int MONEY_REWARD = BossType.MINOTAUR.moneyReward().amount();
  private static final int PENALTY = BossType.MINOTAUR.penalty().amount();

  /** Battle con un solo membro (ID_AVATAR) al turno 0. */
  private Battle stubBattle() {
    Battle b = new Battle(ID_BATTLE, ID_GUILD, BossType.MINOTAUR, 0);
    b.increaseNumOfTurns(ID_AVATAR);
    return b;
  }

  private GuildMember stubMember() {
    return new GuildMember(ID_AVATAR, "Hero", GuildRole.MEMBER);
  }

  // ── POST /api/v1/battles ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/battles")
  class CreateBattle {

    @Test
    @DisplayName("returns 201 with the new battle id")
    void shouldReturn201WithId() throws Exception {
      when(guildService.isLeader(ID_GUILD, ID_LEADER)).thenReturn(true);
      when(guildService.getMembers(ID_GUILD)).thenReturn(List.of(stubMember()));
      when(battleService.createBattle(eq(ID_GUILD), eq(BossType.MINOTAUR), eq(1)))
          .thenReturn(ID_BATTLE);

      mockMvc
          .perform(
              post("/api/v1/battles")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","bossType":"MINOTAUR","requesterId":"leader-1"}
                                      """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(ID_BATTLE.value()));
    }

    @Test
    @DisplayName("delegates guild, boss and turn count (derived from member count) to the service")
    void shouldDelegateToService() throws Exception {
      when(guildService.isLeader(any(Id.class), any(Id.class))).thenReturn(true);
      when(guildService.getMembers(any(Id.class))).thenReturn(List.of(stubMember(), stubMember()));
      when(battleService.createBattle(any(Id.class), any(BossType.class), anyInt()))
          .thenReturn(ID_BATTLE);

      mockMvc
          .perform(
              post("/api/v1/battles")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","bossType":"MINOTAUR","requesterId":"leader-1"}
                                      """))
          .andExpect(status().isCreated());

      verify(battleService).createBattle(eq(ID_GUILD), eq(BossType.MINOTAUR), eq(2));
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
      when(guildService.isLeader(eq(ID_GUILD), eq(new Id<>("not-a-leader")))).thenReturn(false);

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
      when(guildService.isLeader(eq(ID_UNKNOWN_GUILD), eq(ID_LEADER)))
          .thenThrow(new GuildNotFoundException(ID_UNKNOWN_GUILD.value()));

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
      when(battleService.getBattleById(ID_BATTLE)).thenReturn(stubBattle());

      mockMvc.perform(get("/api/v1/battles/{id}", ID_BATTLE.value())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBattleById(ID_UNKNOWN_BATTLE))
          .thenThrow(new BattleNotFoundException(ID_UNKNOWN_BATTLE.value()));

      mockMvc
          .perform(get("/api/v1/battles/{id}", ID_UNKNOWN_BATTLE.value()))
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
      when(guildService.isLeader(ID_GUILD, ID_LEADER)).thenReturn(true);
      doNothing().when(battleService).deleteBattle(ID_BATTLE);

      mockMvc
          .perform(
              delete("/api/v1/battles/{id}", ID_BATTLE.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","requesterId":"leader-1"}
                                      """))
          .andExpect(status().isNoContent());

      verify(battleService).deleteBattle(ID_BATTLE);
    }

    @Test
    @DisplayName("returns 403 when requestor is not the guild leader")
    void shouldReturn403WhenNotLeader() throws Exception {
      when(guildService.isLeader(eq(ID_GUILD), eq(new Id<>("not-a-leader")))).thenReturn(false);

      mockMvc
          .perform(
              delete("/api/v1/battles/{id}", ID_BATTLE.value())
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
      when(guildService.isLeader(ID_GUILD, ID_LEADER)).thenReturn(true);

      mockMvc
          .perform(
              delete("/api/v1/battles/{id}", ID_UNKNOWN_BATTLE.value())
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
      when(battleService.getBattleByGuild(ID_GUILD)).thenReturn(Optional.of(stubBattle()));

      mockMvc
          .perform(get("/api/v1/battles/guild/{guildId}", ID_GUILD.value()))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when no battle exists for the guild")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBattleByGuild(ID_UNKNOWN_GUILD))
          .thenThrow(new BattleNotFoundException(ID_UNKNOWN_GUILD.value()));

      mockMvc
          .perform(get("/api/v1/battles/guild/{guildId}", ID_UNKNOWN_GUILD.value()))
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
      when(battleService.hasBattleInProgress(ID_GUILD)).thenReturn(true);

      mockMvc
          .perform(get("/api/v1/battles/guild/{guildId}/in-progress", ID_GUILD.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.inProgress").value(true));
    }

    @Test
    @DisplayName("returns 200 with inProgress=false when no battle is running")
    void shouldReturnFalseWhenNotInProgress() throws Exception {
      when(battleService.hasBattleInProgress(ID_GUILD)).thenReturn(false);

      mockMvc
          .perform(get("/api/v1/battles/guild/{guildId}/in-progress", ID_GUILD.value()))
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
      when(battleService.getBoss(ID_BATTLE)).thenReturn(BossType.MINOTAUR);

      mockMvc
          .perform(get("/api/v1/battles/{id}/boss", ID_BATTLE.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("MINOTAUR"));
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBoss(ID_UNKNOWN_BATTLE))
          .thenThrow(new BattleNotFoundException(ID_UNKNOWN_BATTLE.value()));

      mockMvc
          .perform(get("/api/v1/battles/{id}/boss", ID_UNKNOWN_BATTLE.value()))
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
      when(battleService.getBossRemainingHealth(ID_BATTLE))
          .thenReturn(new BossStatus(new Health(800)));

      mockMvc
          .perform(get("/api/v1/battles/{id}/boss/health", ID_BATTLE.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.remainingHealth").value(800));
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBossRemainingHealth(ID_UNKNOWN_BATTLE))
          .thenThrow(new BattleNotFoundException(ID_UNKNOWN_BATTLE.value()));

      mockMvc
          .perform(get("/api/v1/battles/{id}/boss/health", ID_UNKNOWN_BATTLE.value()))
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
      when(battleService.getCurrentTurn(ID_BATTLE)).thenReturn(2);

      mockMvc
          .perform(get("/api/v1/battles/{id}/turns/current", ID_BATTLE.value()))
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
      when(battleService.getNumOfTurns(ID_BATTLE)).thenReturn(5);

      mockMvc
          .perform(get("/api/v1/battles/{id}/turns/total", ID_BATTLE.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.turn").value(5));
    }
  }

  // ── POST /api/v1/battles/{id}/damage ─────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/battles/{id}/damage")
  class DealDamage {

    private void stubAttackerTurn() throws BattleNotFoundException {
      when(battleService.isAttackerTurn(ID_BATTLE, ID_AVATAR)).thenReturn(true);
    }

    @Test
    @DisplayName("grants XP and money to all members when boss is killed (Won)")
    void shouldGrantRewardsOnWin() throws Exception {
      stubAttackerTurn();
      when(battleService.dealDamageOnBoss(ID_BATTLE, ID_AVATAR, 500))
          .thenReturn(new BattleOutcome.Won(EXP_REWARD, MONEY_REWARD));
      when(battleService.getBattleById(ID_BATTLE)).thenReturn(stubBattle());

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", ID_BATTLE.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":500,"attackerAvatarId":"avatar-1"}
                                      """))
          .andExpect(status().isNoContent());

      verify(avatarClient).grantExperience(eq(ID_AVATAR.value()), eq(EXP_REWARD));
      verify(avatarClient).earnMoney(eq(ID_AVATAR.value()), eq(MONEY_REWARD));
      verifyNoMoreInteractions(avatarClient);
    }

    @Test
    @DisplayName("applies penalty to all members when counterattack triggers Lost")
    void shouldApplyPenaltyOnLoss() throws Exception {
      stubAttackerTurn();
      when(battleService.dealDamageOnBoss(ID_BATTLE, ID_AVATAR, 10))
          .thenReturn(new BattleOutcome.Ongoing());
      when(avatarClient.applyDamage(ID_AVATAR.value(), 10))
          .thenReturn(new AvatarClient.DamageResult(true));
      when(battleService.applyCounterattack(ID_BATTLE, ID_AVATAR))
          .thenReturn(new BattleOutcome.Lost(PENALTY));
      when(battleService.getBattleById(ID_BATTLE)).thenReturn(stubBattle());

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", ID_BATTLE.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":10,"attackerAvatarId":"avatar-1"}
                                      """))
          .andExpect(status().isNoContent());

      verify(avatarClient).applyPenalty(eq(ID_AVATAR.value()), eq(PENALTY));
    }

    @Test
    @DisplayName(
        "advances turn and applies damage to attacker when boss survives and attacker lives")
    void shouldAdvanceTurnWhenOngoingAndAttackerLives() throws Exception {
      stubAttackerTurn();
      when(battleService.dealDamageOnBoss(ID_BATTLE, ID_AVATAR, 30))
          .thenReturn(new BattleOutcome.Ongoing());
      when(avatarClient.applyDamage(ID_AVATAR.value(), 30))
          .thenReturn(new AvatarClient.DamageResult(false));

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", ID_BATTLE.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":30,"attackerAvatarId":"avatar-1"}
                                      """))
          .andExpect(status().isNoContent());

      verify(avatarClient).applyDamage(ID_AVATAR.value(), 30);
      verify(battleService).nextTurn(ID_BATTLE);
      verify(battleService, never()).applyCounterattack(any(), any());
    }

    @Test
    @DisplayName("applies counterattack and advances turn when attacker dies but guild survives")
    void shouldApplyCounterattackAndAdvanceTurnWhenAttackerDiesButGuildSurvives() throws Exception {
      stubAttackerTurn();
      when(battleService.dealDamageOnBoss(ID_BATTLE, ID_AVATAR, 30))
          .thenReturn(new BattleOutcome.Ongoing());
      when(avatarClient.applyDamage(ID_AVATAR.value(), 30))
          .thenReturn(new AvatarClient.DamageResult(true));
      when(battleService.applyCounterattack(ID_BATTLE, ID_AVATAR))
          .thenReturn(new BattleOutcome.Ongoing());

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", ID_BATTLE.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"damage":30,"attackerAvatarId":"avatar-1"}
                                      """))
          .andExpect(status().isNoContent());

      verify(battleService).applyCounterattack(ID_BATTLE, ID_AVATAR);
      verify(battleService).nextTurn(ID_BATTLE);
    }

    @Test
    @DisplayName("returns 403 when it is not the attacker's turn")
    void shouldReturn403WhenWrongAttacker() throws Exception {
      when(battleService.isAttackerTurn(eq(ID_BATTLE), eq(new Id<>("wrong-avatar"))))
          .thenReturn(false);

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", ID_BATTLE.value())
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
              post("/api/v1/battles/{id}/damage", ID_BATTLE.value())
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
      when(battleService.isAttackerTurn(eq(ID_UNKNOWN_BATTLE), eq(ID_AVATAR)))
          .thenThrow(new BattleNotFoundException(ID_UNKNOWN_BATTLE.value()));

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", ID_UNKNOWN_BATTLE.value())
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
      when(battleService.getBattleStatus(ID_BATTLE)).thenReturn(new BattleOutcome.Ongoing());
      when(battleService.isBattleOver(ID_BATTLE)).thenReturn(false);
      when(battleService.isBattleWon(ID_BATTLE)).thenReturn(false);

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", ID_BATTLE.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isOver").value(false))
          .andExpect(jsonPath("$.isWon").value(false));
    }

    @Test
    @DisplayName("returns 200 with isOver=true and isWon=true when battle is Won")
    void shouldReturnWonStatus() throws Exception {
      when(battleService.getBattleStatus(ID_BATTLE))
          .thenReturn(new BattleOutcome.Won(EXP_REWARD, MONEY_REWARD));
      when(battleService.isBattleOver(ID_BATTLE)).thenReturn(true);
      when(battleService.isBattleWon(ID_BATTLE)).thenReturn(true);

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", ID_BATTLE.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isOver").value(true))
          .andExpect(jsonPath("$.isWon").value(true));
    }

    @Test
    @DisplayName("returns 200 with isOver=true and isWon=false when battle is Lost")
    void shouldReturnLostStatus() throws Exception {
      when(battleService.getBattleStatus(ID_BATTLE)).thenReturn(new BattleOutcome.Lost(PENALTY));
      when(battleService.isBattleOver(ID_BATTLE)).thenReturn(true);
      when(battleService.isBattleWon(ID_BATTLE)).thenReturn(false);

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", ID_BATTLE.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isOver").value(true))
          .andExpect(jsonPath("$.isWon").value(false));
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleService.getBattleStatus(ID_UNKNOWN_BATTLE))
          .thenThrow(new BattleNotFoundException(ID_UNKNOWN_BATTLE.value()));

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", ID_UNKNOWN_BATTLE.value()))
          .andExpect(status().isNotFound());
    }
  }
}
