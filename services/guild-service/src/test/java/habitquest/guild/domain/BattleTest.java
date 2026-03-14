package habitquest.guild.domain;

import static org.assertj.core.api.Assertions.*;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Battle")
class BattleTest {

  private static final String BATTLE_ID = "battle-1";
  private static final String GUILD_ID = "guild-1";
  private static final int BOSS_MAX_HEALTH = 100;
  private static final int NUM_OF_TURNS = 5;

  private Minotaur minotaur;
  private Battle battle;

  @BeforeEach
  void setUp() {
    Stats stats =
        new Stats("stats-1", new Health(BOSS_MAX_HEALTH), new Strength(30), new Defense(10));
    minotaur =
        new Minotaur("Minotaur", stats, new Money(500), new Penalty(50), new Experience(200));
    battle =
        new Battle(
            BATTLE_ID,
            GUILD_ID,
            minotaur,
            NUM_OF_TURNS,
            0,
            new BossStatus(new Health(BOSS_MAX_HEALTH)));
  }

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
    @DisplayName("should start with ONGOING status")
    void shouldStartOngoing() {
      assertThat(battle.getBattleStatus()).isEqualTo(BattleStatus.ONGOING);
    }

    @Test
    @DisplayName("should start at turn zero")
    void shouldStartAtTurnZero() {
      assertThat(battle.getCurrentTurn()).isEqualTo(0);
    }

    @Test
    @DisplayName("should have full boss health")
    void shouldStartWithFullBossHealth() {
      assertThat(battle.getBossRemainingHealth().remainingHealth().value())
          .isEqualTo(BOSS_MAX_HEALTH);
    }
  }

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
      for (int i = 0; i < NUM_OF_TURNS; i++) {
        battle.nextTurn();
      }

      assertThat(battle.getCurrentTurn()).isEqualTo(0);
    }
  }

  @Nested
  @DisplayName("dealDamage")
  class DealDamage {

    @Test
    @DisplayName("should reduce boss health by the damage amount")
    void shouldReduceBossHealth() {
      battle.dealDamage(30);

      assertThat(battle.getBossRemainingHealth().remainingHealth().value()).isEqualTo(70);
    }

    @Test
    @DisplayName("should keep status ONGOING when boss still has health")
    void shouldKeepOngoingWhenBossAlive() {
      battle.dealDamage(50);

      assertThat(battle.getBattleStatus()).isEqualTo(BattleStatus.ONGOING);
    }

    @Test
    @DisplayName("should set status to WON when damage reduces health to zero")
    void shouldSetWonWhenHealthReachesZero() {
      battle.dealDamage(BOSS_MAX_HEALTH);

      assertThat(battle.getBattleStatus()).isEqualTo(BattleStatus.WON);
    }

    @Test
    @DisplayName("should set status to WON when damage exceeds remaining health")
    void shouldSetWonWhenDamageExceedsHealth() {
      battle.dealDamage(BOSS_MAX_HEALTH + 50);

      assertThat(battle.getBattleStatus()).isEqualTo(BattleStatus.WON);
    }

    @Test
    @DisplayName("should set boss health to zero when battle is won")
    void shouldSetHealthToZeroOnWin() {
      battle.dealDamage(BOSS_MAX_HEALTH);

      assertThat(battle.getBossRemainingHealth().remainingHealth().value()).isEqualTo(0);
    }

    @Test
    @DisplayName("should accumulate damage from multiple hits")
    void shouldAccumulateDamage() {
      battle.dealDamage(30);
      battle.dealDamage(30);

      assertThat(battle.getBossRemainingHealth().remainingHealth().value()).isEqualTo(40);
    }
  }

  @Nested
  @DisplayName("setNumOfTurns")
  class SetNumOfTurns {

    @Test
    @DisplayName("should increase the number of turns")
    void shouldIncreaseNumOfTurns() {
      battle.increaseNumOfTurns();
      assertThat(battle.getNumOfTurns()).isEqualTo(6);
    }

    @Test
    @DisplayName("should decrease the number of turns")
    void shouldDecreaseNumOfTurns() {
      battle.increaseNumOfTurns();
      assertThat(battle.getNumOfTurns()).isEqualTo(6);
    }
  }
}
