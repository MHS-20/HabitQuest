package habitquest.guild.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleStatus;
import habitquest.guild.domain.battle.Experience;
import habitquest.guild.domain.battle.Money;
import habitquest.guild.domain.battle.Penalty;
import habitquest.guild.domain.battle.boss.BossStatus;
import habitquest.guild.domain.battle.boss.Minotaur;
import habitquest.guild.domain.battle.stats.Defense;
import habitquest.guild.domain.battle.stats.Health;
import habitquest.guild.domain.battle.stats.Stats;
import habitquest.guild.domain.battle.stats.Strength;
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

  private static final String BATTLE_ID = "battle-1";
  private static final String GUILD_ID = "guild-1";
  private static final int BOSS_HEALTH = 100;
  private static final int NUM_OF_TURNS = 5;

  @Mock private BattleRepository battleRepository;
  @Mock private BattleObserver battleObserver;
  @Mock private BattleFactory battleFactory;

  @InjectMocks private BattleServiceImpl battleService;

  private Minotaur minotaur;
  private Battle battle;

  @BeforeEach
  void setUp() {
    Stats stats = new Stats("stats-1", new Health(BOSS_HEALTH), new Strength(30), new Defense(10));
    minotaur =
        new Minotaur("Minotaur", stats, new Money(500), new Penalty(50), new Experience(200));
    battle =
        new Battle(
            BATTLE_ID,
            GUILD_ID,
            minotaur,
            NUM_OF_TURNS,
            0,
            new BossStatus(new Health(BOSS_HEALTH)));
  }

  // -------------------------------------------------------------------------
  // createBattle
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("createBattle")
  class CreateBattle {

    @Test
    @DisplayName("should delegate to factory, save and return the battle id")
    void shouldCreateAndReturnId() {
      when(battleFactory.create(any(), any(), any())).thenReturn(battle);

      String result = battleService.createBattle(GUILD_ID, minotaur, NUM_OF_TURNS);

      verify(battleRepository).save(battle);
      assertThat(result).isEqualTo(BATTLE_ID);
    }

    @Test
    @DisplayName("should publish a BattleStarted event")
    void shouldPublishBattleStartedEvent() {
      when(battleFactory.create(any(), any(), any())).thenReturn(battle);

      battleService.createBattle(GUILD_ID, minotaur, NUM_OF_TURNS);

      ArgumentCaptor<BattleEvent> captor = ArgumentCaptor.forClass(BattleEvent.class);
      verify(battleObserver).notifyBattleEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(BattleStarted.class);
      assertThat(((BattleStarted) captor.getValue()).battleId()).isEqualTo(BATTLE_ID);
    }
  }

  // -------------------------------------------------------------------------
  // getBattleById
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("getBattleById")
  class GetBattleById {

    @Test
    @DisplayName("should return the battle when it exists")
    void shouldReturnBattleWhenFound() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      Battle result = battleService.getBattleById(BATTLE_ID);

      assertThat(result).isSameAs(battle);
    }

    @Test
    @DisplayName("should throw BattleNotFoundException when battle does not exist")
    void shouldThrowWhenNotFound() {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> battleService.getBattleById(BATTLE_ID))
          .isInstanceOf(BattleNotFoundException.class);
    }
  }

  // -------------------------------------------------------------------------
  // deleteBattle
  // -------------------------------------------------------------------------
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

  // -------------------------------------------------------------------------
  // getBattleByGuild
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("getBattleByGuild")
  class GetBattleByGuild {

    @Test
    @DisplayName("should return the battle for the given guild")
    void shouldReturnBattleForGuild() {
      when(battleRepository.findByGuildId(GUILD_ID)).thenReturn(Optional.of(battle));

      Optional<Battle> result = battleService.getBattleByGuild(GUILD_ID);

      assertThat(result).contains(battle);
    }

    @Test
    @DisplayName("should return empty Optional when no battle exists for the guild")
    void shouldReturnEmptyWhenNotFound() {
      when(battleRepository.findByGuildId(GUILD_ID)).thenReturn(Optional.empty());

      assertThat(battleService.getBattleByGuild(GUILD_ID)).isEmpty();
    }
  }

  // -------------------------------------------------------------------------
  // hasBattleInProgress
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("hasBattleInProgress")
  class HasBattleInProgress {

    @Test
    @DisplayName("should return true when battle status is ONGOING")
    void shouldReturnTrueWhenOngoing() throws BattleNotFoundException {
      when(battleRepository.findByGuildId(GUILD_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.hasBattleInProgress(GUILD_ID)).isTrue();
    }

    @Test
    @DisplayName("should return false when battle is already WON")
    void shouldReturnFalseWhenWon() throws BattleNotFoundException {
      battle.dealDamage(BOSS_HEALTH);
      when(battleRepository.findByGuildId(GUILD_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.hasBattleInProgress(GUILD_ID)).isFalse();
    }
  }

  // -------------------------------------------------------------------------
  // nextTurn / increaseNumOfTurn
  // -------------------------------------------------------------------------
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

      battleService.increaseNumOfTurn(BATTLE_ID);

      assertThat(battle.getNumOfTurns()).isEqualTo(NUM_OF_TURNS + 1);
      verify(battleRepository).save(battle);
    }
  }

  // -------------------------------------------------------------------------
  // dealDamage
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("dealDamage")
  class DealDamage {

    @Test
    @DisplayName("should save the updated battle after damage")
    void shouldSaveBattleAfterDamage() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      battleService.dealDamage(BATTLE_ID, 10);

      verify(battleRepository).save(battle);
    }

    @Test
    @DisplayName("should publish BattleWon event when boss health reaches zero")
    void shouldPublishBattleWonWhenBossDefeated() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      battleService.dealDamage(BATTLE_ID, BOSS_HEALTH);

      ArgumentCaptor<BattleEvent> captor = ArgumentCaptor.forClass(BattleEvent.class);
      verify(battleObserver).notifyBattleEvent(captor.capture());
      BattleWon event = (BattleWon) captor.getValue();
      assertThat(event.battleId()).isEqualTo(BATTLE_ID);
      assertThat(event.experienceReward()).isEqualTo(minotaur.experienceReward());
      assertThat(event.moneyReward()).isEqualTo(minotaur.moneyReward());
    }

    @Test
    @DisplayName("should publish BattleLost event when boss survives")
    void shouldPublishBattleLostWhenBossAlive() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      battleService.dealDamage(BATTLE_ID, 10);

      ArgumentCaptor<BattleEvent> captor = ArgumentCaptor.forClass(BattleEvent.class);
      verify(battleObserver).notifyBattleEvent(captor.capture());
      BattleLost event = (BattleLost) captor.getValue();
      assertThat(event.battleId()).isEqualTo(BATTLE_ID);
      assertThat(event.penalty()).isEqualTo(minotaur.penalty());
    }

    @Test
    @DisplayName("should throw BattleNotFoundException when battle does not exist")
    void shouldThrowWhenBattleNotFound() {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> battleService.dealDamage(BATTLE_ID, 10))
          .isInstanceOf(BattleNotFoundException.class);
    }
  }

  // -------------------------------------------------------------------------
  // isBattleOver / isBattleWon
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("battle status helpers")
  class BattleStatusHelpers {

    @Test
    @DisplayName("isBattleOver should return false when ONGOING")
    void isBattleOverFalseWhenOngoing() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.isBattleOver(BATTLE_ID)).isFalse();
    }

    @Test
    @DisplayName("isBattleOver should return true when WON")
    void isBattleOverTrueWhenWon() throws BattleNotFoundException {
      battle.dealDamage(BOSS_HEALTH);
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.isBattleOver(BATTLE_ID)).isTrue();
    }

    @Test
    @DisplayName("isBattleWon should return false when ONGOING")
    void isBattleWonFalseWhenOngoing() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.isBattleWon(BATTLE_ID)).isFalse();
    }

    @Test
    @DisplayName("isBattleWon should return true when WON")
    void isBattleWonTrueWhenWon() throws BattleNotFoundException {
      battle.dealDamage(BOSS_HEALTH);
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.isBattleWon(BATTLE_ID)).isTrue();
    }

    @Test
    @DisplayName("getBattleStatus should return the correct status")
    void getBattleStatusShouldReturnCorrectStatus() throws BattleNotFoundException {
      when(battleRepository.findById(BATTLE_ID)).thenReturn(Optional.of(battle));

      assertThat(battleService.getBattleStatus(BATTLE_ID)).isEqualTo(BattleStatus.ONGOING);
    }
  }
}
