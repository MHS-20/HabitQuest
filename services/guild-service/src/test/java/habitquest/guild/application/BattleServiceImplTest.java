package habitquest.guild.application;

import static habitquest.guild.GuildFixtures.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import common.ddd.Id;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossType;
import habitquest.guild.domain.events.battleEvents.BattleEvent;
import habitquest.guild.domain.events.battleEvents.BattleLost;
import habitquest.guild.domain.events.battleEvents.BattleObserver;
import habitquest.guild.domain.events.battleEvents.BattleStarted;
import habitquest.guild.domain.events.battleEvents.BattleWon;
import habitquest.guild.domain.factory.BattleFactory;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("BattleServiceImpl")
@ExtendWith(MockitoExtension.class)
class BattleServiceImplTest {

  @Mock private BattleRepository battleRepository;
  @Mock private BattleObserver battleObserver;
  @Mock private BattleFactory battleFactory;

  @InjectMocks private BattleServiceImpl battleService;

  private Battle battle;

  @BeforeEach
  void setUp() {
    battle = battleWithTwoMembers();
  }

  // ── createBattle ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("createBattle")
  class CreateBattle {

    @Test
    @DisplayName("should delegate to factory, save and return the battle id")
    void shouldCreateAndReturnId() {
      when(battleFactory.create(any(), any(), any(), any())).thenReturn(battle);

      Id<Battle> result = battleService.createBattle(GUILD_ID, BossType.MINOTAUR, 2, GUILD_MEMBERS);

      verify(battleRepository).save(battle);
      assertThat(result).isEqualTo(BATTLE_ID);
    }

    @Test
    @DisplayName("should publish a BattleStarted event")
    void shouldPublishBattleStartedEvent() {
      when(battleFactory.create(any(), any(), any(), any())).thenReturn(battle);

      battleService.createBattle(GUILD_ID, BossType.MINOTAUR, 2, GUILD_MEMBERS);

      ArgumentCaptor<BattleEvent> captor = ArgumentCaptor.forClass(BattleEvent.class);
      verify(battleObserver).notifyBattleEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(BattleStarted.class);
      assertThat(((BattleStarted) captor.getValue()).battleId().value())
          .isEqualTo(BATTLE_ID.value());
    }
  }

  // ── getBattleById ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getBattleById")
  class GetBattleById {

    @Test
    @DisplayName("should return the battle when it exists")
    void shouldReturnBattleWhenFound() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.getBattleById(BATTLE_ID)).isSameAs(battle);
    }

    @Test
    @DisplayName("should throw BattleNotFoundException when battle does not exist")
    void shouldThrowWhenNotFound() {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> battleService.getBattleById(BATTLE_ID))
          .isInstanceOf(BattleNotFoundException.class);
    }
  }

  // ── deleteBattle ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("deleteBattle")
  class DeleteBattle {

    @Test
    @DisplayName("should delete the battle from repository")
    void shouldDeleteBattle() {
      battleService.deleteBattle(BATTLE_ID);
      verify(battleRepository).deleteById(BATTLE_ID);
    }
  }

  // ── getBattleByGuild ─────────────────────────────────────────────────────────

  @Nested
  @DisplayName("getBattleByGuild")
  class GetBattleByGuild {

    @Test
    @DisplayName("should return the battle for the given guild")
    void shouldReturnBattleForGuild() {
      when(battleRepository.findByGuildId(GUILD_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.getBattleByGuild(GUILD_ID)).contains(battle);
    }

    @Test
    @DisplayName("should return empty Optional when no battle exists for the guild")
    void shouldReturnEmptyWhenNotFound() {
      when(battleRepository.findByGuildId(GUILD_ID)).thenReturn(Optional.empty());

      assertThat(battleService.getBattleByGuild(GUILD_ID)).isEmpty();
    }
  }

  // ── hasBattleInProgress ───────────────────────────────────────────────────────

  @Nested
  @DisplayName("hasBattleInProgress")
  class HasBattleInProgress {

    @Test
    @DisplayName("should return true when battle status is Ongoing")
    void shouldReturnTrueWhenOngoing() throws BattleNotFoundException {
      when(battleRepository.findByGuildId(GUILD_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.hasBattleInProgress(GUILD_ID)).isTrue();
    }

    @Test
    @DisplayName("should return false when battle is Won")
    void shouldReturnFalseWhenWon() throws BattleNotFoundException {
      battle.dealDamageOnBoss(BATTLE_MEMBER_ID_1, BOSS_HEALTH);
      when(battleRepository.findByGuildId(GUILD_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.hasBattleInProgress(GUILD_ID)).isFalse();
    }

    @Test
    @DisplayName("should return false when battle is Lost")
    void shouldReturnFalseWhenLost() throws BattleNotFoundException {
      battle.applyCounterattack(BATTLE_MEMBER_ID_1);
      battle.applyCounterattack(BATTLE_MEMBER_ID_2);
      when(battleRepository.findByGuildId(GUILD_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.hasBattleInProgress(GUILD_ID)).isFalse();
    }
  }

  // ── turn management ───────────────────────────────────────────────────────────

  @Nested
  @DisplayName("turn management")
  class TurnManagement {

    @Test
    @DisplayName("nextTurn should advance turn and save")
    void nextTurnShouldAdvanceAndSave() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      battleService.nextTurn(BATTLE_ID);

      assertThat(battle.getCurrentTurn()).isEqualTo(1);
      verify(battleRepository).save(battle);
    }

    @Test
    @DisplayName("increaseNumOfTurn should increment numOfTurns and save")
    void increaseNumOfTurnShouldIncrementAndSave() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));
      int before = battle.getNumOfTurns();

      battleService.increaseNumOfTurn(BATTLE_ID, new Id<>("member-3"));

      assertThat(battle.getNumOfTurns()).isEqualTo(before + 1);
      verify(battleRepository).save(battle);
    }
  }

  // ── dealDamageOnBoss ──────────────────────────────────────────────────────────

  @Nested
  @DisplayName("dealDamageOnBoss")
  class DealDamageOnBoss {

    @Test
    @DisplayName("should return Ongoing and save when boss survives")
    void shouldReturnOngoingAndSaveWhenBossSurvives() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      BattleOutcome outcome = battleService.dealDamageOnBoss(BATTLE_ID, BATTLE_MEMBER_ID_1, 10);

      assertThat(outcome).isInstanceOf(BattleOutcome.Ongoing.class);
      verify(battleRepository).save(battle);
      verifyNoInteractions(battleObserver);
    }

    @Test
    @DisplayName("should return Won, publish BattleWon and delete when boss health reaches zero")
    void shouldReturnWonAndPublishEventWhenBossDefeated() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      BattleOutcome outcome =
          battleService.dealDamageOnBoss(BATTLE_ID, BATTLE_MEMBER_ID_1, BOSS_HEALTH);

      assertThat(outcome).isInstanceOf(BattleOutcome.Won.class);
      BattleOutcome.Won won = (BattleOutcome.Won) outcome;
      assertThat(won.experienceReward()).isEqualTo(EXP_REWARD);
      assertThat(won.moneyReward()).isEqualTo(MONEY_REWARD);

      ArgumentCaptor<BattleEvent> captor = ArgumentCaptor.forClass(BattleEvent.class);
      verify(battleObserver).notifyBattleEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(BattleWon.class);
      BattleWon event = (BattleWon) captor.getValue();
      assertThat(event.battleId().value()).isEqualTo(BATTLE_ID.value());
      assertThat(event.guildId().value()).isEqualTo(GUILD_ID.value());
      assertThat(event.experienceReward()).isEqualTo(EXP_REWARD);
      assertThat(event.moneyReward()).isEqualTo(MONEY_REWARD);

      // il service chiama deleteById, non save, quando vince
      verify(battleRepository).deleteById(BATTLE_ID);
      verify(battleRepository, never()).save(any());
    }

    @Test
    @DisplayName("should throw BattleNotFoundException when battle does not exist")
    void shouldThrowWhenBattleNotFound() {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> battleService.dealDamageOnBoss(BATTLE_ID, BATTLE_MEMBER_ID_1, 10))
          .isInstanceOf(BattleNotFoundException.class);
    }
  }

  // ── applyCounterattack ────────────────────────────────────────────────────────

  @Nested
  @DisplayName("applyCounterattack")
  class ApplyCounterattack {

    @Test
    @DisplayName("should return Ongoing and save when not all members have fallen")
    void shouldReturnOngoingAndSaveWhenNotAllFallen() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      BattleOutcome outcome = battleService.applyCounterattack(BATTLE_ID, BATTLE_MEMBER_ID_1);

      assertThat(outcome).isInstanceOf(BattleOutcome.Ongoing.class);
      verify(battleRepository).save(battle);
      verifyNoInteractions(battleObserver);
    }

    @Test
    @DisplayName("should return Lost, publish BattleLost and delete when all members have fallen")
    void shouldReturnLostAndPublishEventWhenAllFallen() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));
      battleService.applyCounterattack(BATTLE_ID, BATTLE_MEMBER_ID_1); // Ongoing

      BattleOutcome outcome =
          battleService.applyCounterattack(BATTLE_ID, BATTLE_MEMBER_ID_2); // Lost

      assertThat(outcome).isInstanceOf(BattleOutcome.Lost.class);
      assertThat(((BattleOutcome.Lost) outcome).penalty()).isEqualTo(PENALTY);

      ArgumentCaptor<BattleEvent> captor = ArgumentCaptor.forClass(BattleEvent.class);
      verify(battleObserver).notifyBattleEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(BattleLost.class);
      BattleLost event = (BattleLost) captor.getValue();
      assertThat(event.battleId()).isEqualTo(BATTLE_ID);
      assertThat(event.penalty()).isEqualTo(PENALTY);

      // primo counterattack → save; secondo → deleteById
      verify(battleRepository).save(battle);
      verify(battleRepository).deleteById(BATTLE_ID);
    }

    @Test
    @DisplayName("should throw BattleNotFoundException when battle does not exist")
    void shouldThrowWhenBattleNotFound() {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> battleService.applyCounterattack(BATTLE_ID, BATTLE_MEMBER_ID_1))
          .isInstanceOf(BattleNotFoundException.class);
    }
  }

  // ── markAsFallen ─────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("markAsFallen")
  class MarkAsFallen {

    @Test
    @DisplayName("should return Ongoing when not all members have fallen")
    void shouldReturnOngoingWhenNotAllFallen() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      BattleOutcome outcome = battleService.markAsFallen(BATTLE_ID, BATTLE_MEMBER_ID_1);

      assertThat(outcome).isInstanceOf(BattleOutcome.Ongoing.class);
      verify(battleRepository).save(battle);
      verifyNoInteractions(battleObserver);
    }

    @Test
    @DisplayName("should return Lost and publish BattleLost when all members have fallen")
    void shouldReturnLostAndPublishEventWhenAllFallen() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));
      battleService.markAsFallen(BATTLE_ID, BATTLE_MEMBER_ID_1);

      BattleOutcome outcome = battleService.markAsFallen(BATTLE_ID, BATTLE_MEMBER_ID_2);

      assertThat(outcome).isInstanceOf(BattleOutcome.Lost.class);

      ArgumentCaptor<BattleEvent> captor = ArgumentCaptor.forClass(BattleEvent.class);
      verify(battleObserver).notifyBattleEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(BattleLost.class);
    }
  }

  // ── isBattleOver / isBattleWon / getBattleStatus ─────────────────────────────

  @Nested
  @DisplayName("battle status helpers")
  class BattleStatusHelpers {

    @Test
    @DisplayName("isBattleOver should return false when Ongoing")
    void isBattleOverFalseWhenOngoing() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.isBattleOver(BATTLE_ID)).isFalse();
    }

    @Test
    @DisplayName("isBattleOver should return true when Won")
    void isBattleOverTrueWhenWon() throws BattleNotFoundException {
      battle.dealDamageOnBoss(BATTLE_MEMBER_ID_1, BOSS_HEALTH);
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.isBattleOver(BATTLE_ID)).isTrue();
    }

    @Test
    @DisplayName("isBattleOver should return true when Lost")
    void isBattleOverTrueWhenLost() throws BattleNotFoundException {
      battle.applyCounterattack(BATTLE_MEMBER_ID_1);
      battle.applyCounterattack(BATTLE_MEMBER_ID_2);
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.isBattleOver(BATTLE_ID)).isTrue();
    }

    @Test
    @DisplayName("isBattleWon should return false when Ongoing")
    void isBattleWonFalseWhenOngoing() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.isBattleWon(BATTLE_ID)).isFalse();
    }

    @Test
    @DisplayName("isBattleWon should return true when Won")
    void isBattleWonTrueWhenWon() throws BattleNotFoundException {
      battle.dealDamageOnBoss(BATTLE_MEMBER_ID_1, BOSS_HEALTH);
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.isBattleWon(BATTLE_ID)).isTrue();
    }

    @Test
    @DisplayName("isBattleWon should return false when Lost")
    void isBattleWonFalseWhenLost() throws BattleNotFoundException {
      battle.applyCounterattack(BATTLE_MEMBER_ID_1);
      battle.applyCounterattack(BATTLE_MEMBER_ID_2);
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.isBattleWon(BATTLE_ID)).isFalse();
    }

    @Test
    @DisplayName("getBattleStatus should return Ongoing initially")
    void getBattleStatusShouldReturnOngoingInitially() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.getBattleStatus(BATTLE_ID))
          .isInstanceOf(BattleOutcome.Ongoing.class);
    }

    @Test
    @DisplayName("getBattleStatus should return Won after boss is defeated")
    void getBattleStatusShouldReturnWonAfterBossDefeated() throws BattleNotFoundException {
      battle.dealDamageOnBoss(BATTLE_MEMBER_ID_1, BOSS_HEALTH);
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.getBattleStatus(BATTLE_ID)).isInstanceOf(BattleOutcome.Won.class);
    }

    @Test
    @DisplayName("getBattleStatus should return Lost after all members fall")
    void getBattleStatusShouldReturnLostAfterAllFallen() throws BattleNotFoundException {
      battle.applyCounterattack(BATTLE_MEMBER_ID_1);
      battle.applyCounterattack(BATTLE_MEMBER_ID_2);
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.getBattleStatus(BATTLE_ID)).isInstanceOf(BattleOutcome.Lost.class);
    }
  }
}
