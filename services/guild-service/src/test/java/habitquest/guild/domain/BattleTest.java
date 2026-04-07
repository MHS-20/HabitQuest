package habitquest.guild.domain;

import static habitquest.guild.GuildFixtures.*;
import static org.assertj.core.api.Assertions.*;

import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Battle")
class BattleTest {

  private Battle battle;

  @BeforeEach
  void setUp() {
    battle = fullBattleFixture();
  }

  // -------------------------------------------------------------------------
  // creation
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("creation")
  class Creation {

    @Test
    @DisplayName("should have correct id")
    void shouldHaveCorrectId() {
      assertThat(battle.getId()).isEqualTo(BATTLE_ID);
    }

    @Test
    @DisplayName("should have correct guildId")
    void shouldHaveCorrectGuildId() {
      assertThat(battle.getGuildId()).isEqualTo(GUILD_ID);
    }

    @Test
    @DisplayName("should start with Ongoing status")
    void shouldStartOngoing() {
      assertThat(battle.getBattleStatus()).isInstanceOf(BattleOutcome.Ongoing.class);
    }

    @Test
    @DisplayName("should start at turn zero")
    void shouldStartAtTurnZero() {
      assertThat(battle.getCurrentTurn()).isEqualTo(0);
    }

    @Test
    @DisplayName("should start with full boss health")
    void shouldStartWithFullBossHealth() {
      assertThat(battle.getBossRemainingHealth().remainingHealth().value())
          .isEqualTo(BOSS_MAX_HEALTH);
    }
  }

  // -------------------------------------------------------------------------
  // nextTurn
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("nextTurn")
  class NextTurn {

    @Test
    @DisplayName("should advance the turn counter")
    void shouldAdvanceTurnCounter() {
      battle.nextTurn();
      assertThat(battle.getCurrentTurn()).isEqualTo(1);
    }

    @Test
    @DisplayName("should wrap around when reaching numOfTurns")
    void shouldWrapAround() {
      // 3 nextTurn su 3 membri → torna a 0
      battle.nextTurn();
      battle.nextTurn();
      battle.nextTurn();
      assertThat(battle.getCurrentTurn()).isEqualTo(0);
    }
  }

  // -------------------------------------------------------------------------
  // dealDamageOnBoss
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("dealDamageOnBoss")
  class DealDamageOnBoss {

    @Test
    @DisplayName("should reduce boss health by the damage amount")
    void shouldReduceBossHealth() {
      battle.dealDamageOnBoss(MEMBER_ID, 30);
      assertThat(battle.getBossRemainingHealth().remainingHealth().value()).isEqualTo(70);
    }

    @Test
    @DisplayName(
        "should return Ongoing and keep internal status Ongoing when boss still has health")
    void shouldReturnOngoingWhenBossAlive() {
      BattleOutcome outcome = battle.dealDamageOnBoss(MEMBER_ID, 50);

      assertThat(outcome).isInstanceOf(BattleOutcome.Ongoing.class);
      assertThat(battle.getBattleStatus()).isInstanceOf(BattleOutcome.Ongoing.class);
    }

    @Test
    @DisplayName("should return Won with correct rewards when damage reduces health to zero")
    void shouldReturnWonWhenHealthReachesZero() {
      BattleOutcome outcome = battle.dealDamageOnBoss(MEMBER_ID, BOSS_MAX_HEALTH);

      assertThat(outcome).isInstanceOf(BattleOutcome.Won.class);
      BattleOutcome.Won won = (BattleOutcome.Won) outcome;
      assertThat(won.experienceReward()).isEqualTo(EXP_REWARD);
      assertThat(won.moneyReward()).isEqualTo(MONEY_REWARD);
    }

    @Test
    @DisplayName("should return Won when damage exceeds remaining health")
    void shouldReturnWonWhenDamageExceedsHealth() {
      assertThat(battle.dealDamageOnBoss(MEMBER_ID, BOSS_MAX_HEALTH + 50))
          .isInstanceOf(BattleOutcome.Won.class);
    }

    @Test
    @DisplayName("should set boss health to zero when battle is won")
    void shouldSetHealthToZeroOnWin() {
      battle.dealDamageOnBoss(MEMBER_ID, BOSS_MAX_HEALTH);
      assertThat(battle.getBossRemainingHealth().remainingHealth().value()).isEqualTo(0);
    }

    @Test
    @DisplayName("should accumulate damage from multiple hits")
    void shouldAccumulateDamage() {
      battle.dealDamageOnBoss(MEMBER_ID, 30);
      battle.dealDamageOnBoss(MEMBER_ID_2, 30);
      assertThat(battle.getBossRemainingHealth().remainingHealth().value()).isEqualTo(40);
    }

    @Test
    @DisplayName("should update internal battleStatus to Won after killing blow")
    void shouldUpdateInternalStatusToWon() {
      battle.dealDamageOnBoss(MEMBER_ID, BOSS_MAX_HEALTH);
      assertThat(battle.getBattleStatus()).isInstanceOf(BattleOutcome.Won.class);
    }
  }

  // -------------------------------------------------------------------------
  // applyCounterattack
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("applyCounterattack")
  class ApplyCounterattack {

    @Test
    @DisplayName("should return Ongoing when not all members have fallen")
    void shouldReturnOngoingWhenNotAllFallen() {
      assertThat(battle.applyCounterattack(MEMBER_ID)).isInstanceOf(BattleOutcome.Ongoing.class);
    }

    @Test
    @DisplayName("should return Lost with correct penalty when all members have fallen")
    void shouldReturnLostWhenAllFallen() {
      battle.applyCounterattack(MEMBER_ID);
      battle.applyCounterattack(MEMBER_ID_2);
      BattleOutcome outcome = battle.applyCounterattack(MEMBER_ID_3);

      assertThat(outcome).isInstanceOf(BattleOutcome.Lost.class);
      assertThat(((BattleOutcome.Lost) outcome).penalty()).isEqualTo(PENALTY);
    }

    @Test
    @DisplayName("should update internal battleStatus to Lost when all members have fallen")
    void shouldUpdateInternalStatusToLost() {
      battle.applyCounterattack(MEMBER_ID);
      battle.applyCounterattack(MEMBER_ID_2);
      battle.applyCounterattack(MEMBER_ID_3);

      assertThat(battle.getBattleStatus()).isInstanceOf(BattleOutcome.Lost.class);
    }
  }

  // -------------------------------------------------------------------------
  // increaseNumOfTurns / decreaseNumOfTurns
  // -------------------------------------------------------------------------
  @Nested
  @DisplayName("numOfTurns management")
  class NumOfTurnsManagement {

    @Test
    @DisplayName("should increase the number of turns and add member")
    void shouldIncreaseNumOfTurns() {
      int before = battle.getNumOfTurns();
      battle.increaseNumOfTurns(MEMBER_ID_4);

      assertThat(battle.getNumOfTurns()).isEqualTo(before + 1);
      assertThat(battle.getMembers()).contains(MEMBER_ID_4);
    }

    @Test
    @DisplayName("should decrease the number of turns and remove member")
    void shouldDecreaseNumOfTurns() {
      int before = battle.getNumOfTurns();
      battle.decreaseNumOfTurns(MEMBER_ID);

      assertThat(battle.getNumOfTurns()).isEqualTo(before - 1);
      assertThat(battle.getMembers()).doesNotContain(MEMBER_ID);
    }
  }
}
