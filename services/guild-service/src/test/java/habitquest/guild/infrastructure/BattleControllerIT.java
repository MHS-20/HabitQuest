package habitquest.guild.infrastructure;

import static habitquest.guild.GuildFixtures.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.ddd.Id;
import habitquest.guild.application.exceptions.BattleNotFoundException;
import habitquest.guild.application.exceptions.GuildNotFoundException;
import habitquest.guild.application.port.in.BattleCommandService;
import habitquest.guild.application.port.in.BattleQueryService;
import habitquest.guild.application.port.in.GuildQueryService;
import habitquest.guild.application.port.out.GuildLogger;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.battle.boss.BossType;
import habitquest.guild.domain.battle.stats.Health;
import habitquest.guild.infrastructure.dto.BattleQueries.*;
import habitquest.guild.infrastructure.dto.BattleResponseAssembler;
import habitquest.guild.infrastructure.inbound.BattleCommandController;
import habitquest.guild.infrastructure.inbound.BattleQueryController;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest({BattleCommandController.class, BattleQueryController.class})
@AutoConfigureMockMvc(addFilters = false)
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
@DisplayName("BattleController")
public class BattleControllerIT {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private BattleCommandService battleCommandService;
  @MockitoBean private BattleQueryService battleQueryService;
  @MockitoBean private GuildQueryService guildQueryService;
  @MockitoBean private BattleResponseAssembler assembler;
  @MockitoBean private GuildLogger log;

  private <T> EntityModel<T> bare(T body) {
    return EntityModel.of(body);
  }

  // ── POST /api/v1/battles ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("POST /api/v1/battles")
  class CreateBattle {

    @Test
    @DisplayName("returns 201 with the new battle id")
    void shouldReturn201WithId() throws Exception {
      when(guildQueryService.isLeader(GUILD_ID, LEADER_HTTP)).thenReturn(true);
      when(guildQueryService.getMembers(GUILD_ID)).thenReturn(List.of(avatarMember()));
      when(battleCommandService.createBattle(eq(GUILD_ID), eq(BossType.MINOTAUR), eq(1), any()))
          .thenReturn(BATTLE_ID);
      when(assembler.toCreatedModel(any())).thenAnswer(inv -> bare(inv.getArgument(0)));

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
      when(guildQueryService.isLeader(any(Id.class), any(Id.class))).thenReturn(true);
      when(guildQueryService.getMembers(any(Id.class)))
          .thenReturn(List.of(avatarMember(), avatarMember()));
      when(battleCommandService.createBattle(any(Id.class), any(BossType.class), anyInt(), any()))
          .thenReturn(BATTLE_ID);
      when(assembler.toCreatedModel(any())).thenAnswer(inv -> bare(inv.getArgument(0)));

      mockMvc
          .perform(
              post("/api/v1/battles")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","bossType":"MINOTAUR","requesterId":"leader-1"}
                                      """))
          .andExpect(status().isCreated());

      verify(battleCommandService).createBattle(eq(GUILD_ID), eq(BossType.MINOTAUR), eq(2), any());
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

      verifyNoInteractions(battleCommandService, battleQueryService);
      verifyNoInteractions(assembler);
    }

    @Test
    @DisplayName("returns 403 when requestor is not the guild leader")
    void shouldReturn403WhenNotLeader() throws Exception {
      Id<habitquest.guild.domain.guild.GuildMember> notLeader = new Id<>("not-a-leader");
      when(guildQueryService.isLeader(eq(GUILD_ID), eq(notLeader))).thenReturn(false);

      mockMvc
          .perform(
              post("/api/v1/battles")
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","bossType":"MINOTAUR","requesterId":"not-a-leader"}
                                      """))
          .andExpect(status().isForbidden());

      verifyNoInteractions(battleCommandService, battleQueryService);
      verifyNoInteractions(assembler);
    }

    @Test
    @DisplayName("returns 404 when guild does not exist")
    void shouldReturn404WhenGuildNotFound() throws Exception {
      when(guildQueryService.isLeader(eq(UNKNOWN_GUILD_ID), eq(LEADER_HTTP)))
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

      verifyNoInteractions(battleCommandService, battleQueryService);
      verifyNoInteractions(assembler);
    }
  }

  // ── GET /api/v1/battles/{id} ──────────────────────────────────────────────────

  @Nested
  @DisplayName("GET /api/v1/battles/{id}")
  class GetBattle {

    @Test
    @DisplayName("returns 200 with battle data when found")
    void shouldReturn200WhenFound() throws Exception {
      when(battleQueryService.getBattleById(BATTLE_ID)).thenReturn(battle());
      when(assembler.toModel(any(BattleResponse.class)))
          .thenAnswer(inv -> bare(inv.getArgument(0)));

      mockMvc.perform(get("/api/v1/battles/{id}", BATTLE_ID.value())).andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleQueryService.getBattleById(UNKNOWN_BATTLE_ID))
          .thenThrow(new BattleNotFoundException(UNKNOWN_BATTLE_ID.value()));

      mockMvc
          .perform(get("/api/v1/battles/{id}", UNKNOWN_BATTLE_ID.value()))
          .andExpect(status().isNotFound());

      verifyNoInteractions(assembler);
    }
  }

  // ── DELETE /api/v1/battles/{id} ───────────────────────────────────────────────

  @Nested
  @DisplayName("DELETE /api/v1/battles/{id}")
  class DeleteBattle {

    @Test
    @DisplayName("returns 204 when leader deletes a battle")
    void shouldReturn204() throws Exception {
      when(guildQueryService.isLeader(GUILD_ID, LEADER_HTTP)).thenReturn(true);
      doNothing().when(battleCommandService).deleteBattle(BATTLE_ID);

      mockMvc
          .perform(
              delete("/api/v1/battles/{id}", BATTLE_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","requesterId":"leader-1"}
                                      """))
          .andExpect(status().isNoContent());

      verify(battleCommandService).deleteBattle(BATTLE_ID);
      verifyNoInteractions(assembler);
    }

    @Test
    @DisplayName("returns 403 when requestor is not the guild leader")
    void shouldReturn403WhenNotLeader() throws Exception {
      Id<habitquest.guild.domain.guild.GuildMember> notLeader = new Id<>("not-a-leader");
      when(guildQueryService.isLeader(eq(GUILD_ID), eq(notLeader))).thenReturn(false);

      mockMvc
          .perform(
              delete("/api/v1/battles/{id}", BATTLE_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                                      {"guildId":"guild-1","requesterId":"not-a-leader"}
                                      """))
          .andExpect(status().isForbidden());

      verify(battleCommandService, never()).deleteBattle(any());
      verifyNoInteractions(assembler);
    }

    @Test
    @DisplayName("returns 204 even when battle does not exist (idempotent delete)")
    void shouldReturn204WhenNotFound() throws Exception {
      when(guildQueryService.isLeader(GUILD_ID, LEADER_HTTP)).thenReturn(true);

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
      when(battleQueryService.getBattleByGuild(GUILD_ID)).thenReturn(Optional.of(battle()));
      when(assembler.toModelForGuild(any(BattleResponse.class), eq(GUILD_ID.value())))
          .thenAnswer(inv -> bare(inv.getArgument(0)));

      mockMvc
          .perform(get("/api/v1/battles/guild/{guildId}", GUILD_ID.value()))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 404 when no battle exists for the guild")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleQueryService.getBattleByGuild(UNKNOWN_GUILD_ID))
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
      when(battleQueryService.hasBattleInProgress(GUILD_ID)).thenReturn(true);
      when(assembler.toInProgressModel(any(InProgressResponse.class), eq(GUILD_ID.value())))
          .thenAnswer(inv -> bare(inv.getArgument(0)));

      mockMvc
          .perform(get("/api/v1/battles/guild/{guildId}/in-progress", GUILD_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.inProgress").value(true));
    }

    @Test
    @DisplayName("returns 200 with inProgress=false when no battle is running")
    void shouldReturnFalseWhenNotInProgress() throws Exception {
      when(battleQueryService.hasBattleInProgress(GUILD_ID)).thenReturn(false);
      when(assembler.toInProgressModel(any(InProgressResponse.class), eq(GUILD_ID.value())))
          .thenAnswer(inv -> bare(inv.getArgument(0)));

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
      when(battleQueryService.getBoss(BATTLE_ID)).thenReturn(BossType.MINOTAUR);
      when(assembler.toBossModel(any(BossResponse.class), eq(BATTLE_ID.value())))
          .thenAnswer(inv -> bare(inv.getArgument(0)));

      mockMvc
          .perform(get("/api/v1/battles/{id}/boss", BATTLE_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name").value("MINOTAUR"));
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleQueryService.getBoss(UNKNOWN_BATTLE_ID))
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
      when(battleQueryService.getBossRemainingHealth(BATTLE_ID))
          .thenReturn(new BossStatus(new Health(800)));
      when(assembler.toBossHealthModel(any(BossHealthResponse.class), eq(BATTLE_ID.value())))
          .thenAnswer(inv -> bare(inv.getArgument(0)));

      mockMvc
          .perform(get("/api/v1/battles/{id}/boss/health", BATTLE_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.remainingHealth").value(800));
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleQueryService.getBossRemainingHealth(UNKNOWN_BATTLE_ID))
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
      when(battleQueryService.getCurrentTurn(BATTLE_ID)).thenReturn(2);
      when(assembler.toCurrentTurnModel(any(TurnResponse.class), eq(BATTLE_ID.value())))
          .thenAnswer(inv -> bare(inv.getArgument(0)));

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
      when(battleQueryService.getNumOfTurns(BATTLE_ID)).thenReturn(5);
      when(assembler.toTotalTurnsModel(any(TurnResponse.class), eq(BATTLE_ID.value())))
          .thenAnswer(inv -> bare(inv.getArgument(0)));

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
      when(battleQueryService.isAttackerTurn(BATTLE_ID, LEADER_ID)).thenReturn(true);
    }

    @Test
    @DisplayName("returns 204 when boss is killed (Won)")
    void shouldReturn204OnWin() throws Exception {
      stubAttackerTurn();
      when(battleCommandService.processDamage(BATTLE_ID, LEADER_ID, 500))
          .thenReturn(new BattleOutcome.Won(EXP_REWARD, MONEY_REWARD));

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", BATTLE_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"damage":500,"attackerAvatarId":"avatar-1"}
                      """))
          .andExpect(status().isNoContent());

      verify(battleCommandService).processDamage(BATTLE_ID, LEADER_ID, 500);
    }

    @Test
    @DisplayName("returns 204 when battle ends in loss")
    void shouldReturn204OnLoss() throws Exception {
      stubAttackerTurn();
      when(battleCommandService.processDamage(BATTLE_ID, LEADER_ID, 10))
          .thenReturn(new BattleOutcome.Lost(PENALTY));

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", BATTLE_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"damage":10,"attackerAvatarId":"avatar-1"}
                      """))
          .andExpect(status().isNoContent());

      verify(battleCommandService).processDamage(BATTLE_ID, LEADER_ID, 10);
    }

    @Test
    @DisplayName("returns 204 when battle is ongoing")
    void shouldReturn204WhenOngoing() throws Exception {
      stubAttackerTurn();
      when(battleCommandService.processDamage(BATTLE_ID, LEADER_ID, 30))
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

      verify(battleCommandService).processDamage(BATTLE_ID, LEADER_ID, 30);
    }

    @Test
    @DisplayName("returns 403 when it is not the attacker's turn")
    void shouldReturn403WhenWrongAttacker() throws Exception {
      Id<habitquest.guild.domain.guild.GuildMember> wrongAvatar = new Id<>("wrong-avatar");
      when(battleQueryService.isAttackerTurn(eq(BATTLE_ID), eq(wrongAvatar))).thenReturn(false);

      mockMvc
          .perform(
              post("/api/v1/battles/{id}/damage", BATTLE_ID.value())
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(
                      """
                      {"damage":30,"attackerAvatarId":"wrong-avatar"}
                      """))
          .andExpect(status().isForbidden());

      verify(battleCommandService, never()).processDamage(any(), any(), anyInt());
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

      verifyNoInteractions(battleCommandService, battleQueryService);
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenBattleNotFound() throws Exception {
      when(battleQueryService.isAttackerTurn(eq(UNKNOWN_BATTLE_ID), eq(LEADER_ID)))
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
      when(battleQueryService.getBattleStatus(BATTLE_ID)).thenReturn(new BattleOutcome.Ongoing());
      when(battleQueryService.isBattleOver(BATTLE_ID)).thenReturn(false);
      when(battleQueryService.isBattleWon(BATTLE_ID)).thenReturn(false);
      when(assembler.toStatusModel(any(BattleStatusResponse.class), eq(BATTLE_ID.value())))
          .thenAnswer(inv -> bare(inv.getArgument(0)));

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", BATTLE_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isOver").value(false))
          .andExpect(jsonPath("$.isWon").value(false));
    }

    @Test
    @DisplayName("returns 200 with isOver=true and isWon=true when battle is Won")
    void shouldReturnWonStatus() throws Exception {
      when(battleQueryService.getBattleStatus(BATTLE_ID))
          .thenReturn(new BattleOutcome.Won(EXP_REWARD, MONEY_REWARD));
      when(battleQueryService.isBattleOver(BATTLE_ID)).thenReturn(true);
      when(battleQueryService.isBattleWon(BATTLE_ID)).thenReturn(true);
      when(assembler.toStatusModel(any(BattleStatusResponse.class), eq(BATTLE_ID.value())))
          .thenAnswer(inv -> bare(inv.getArgument(0)));

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", BATTLE_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isOver").value(true))
          .andExpect(jsonPath("$.isWon").value(true));
    }

    @Test
    @DisplayName("returns 200 with isOver=true and isWon=false when battle is Lost")
    void shouldReturnLostStatus() throws Exception {
      when(battleQueryService.getBattleStatus(BATTLE_ID))
          .thenReturn(new BattleOutcome.Lost(PENALTY));
      when(battleQueryService.isBattleOver(BATTLE_ID)).thenReturn(true);
      when(battleQueryService.isBattleWon(BATTLE_ID)).thenReturn(false);
      when(assembler.toStatusModel(any(BattleStatusResponse.class), eq(BATTLE_ID.value())))
          .thenAnswer(inv -> bare(inv.getArgument(0)));

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", BATTLE_ID.value()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.isOver").value(true))
          .andExpect(jsonPath("$.isWon").value(false));
    }

    @Test
    @DisplayName("returns 404 when battle does not exist")
    void shouldReturn404WhenNotFound() throws Exception {
      when(battleQueryService.getBattleStatus(UNKNOWN_BATTLE_ID))
          .thenThrow(new BattleNotFoundException(UNKNOWN_BATTLE_ID.value()));

      mockMvc
          .perform(get("/api/v1/battles/{id}/status", UNKNOWN_BATTLE_ID.value()))
          .andExpect(status().isNotFound());
    }
  }
}
