package habitquest.guild.infrastructure.dto;

import common.cqrs.QueryResponse;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossEnemy;

public class BattleQueries {
  // Query responses
  public record BossHealthResponse(int remainingHealth) implements QueryResponse {}

  public record TurnResponse(Integer turn) implements QueryResponse {}

  public record InProgressResponse(boolean inProgress) implements QueryResponse {}

  public record BattleStatusResponse(BattleOutcome status, boolean isOver, boolean isWon)
      implements QueryResponse {}

  public record BattleResponse(
      String id,
      String guildId,
      BattleOutcome status,
      int currentTurn,
      int numOfTurns,
      BossResponse boss,
      int bossRemainingHealth)
      implements QueryResponse {

    public static BattleResponse from(Battle battle) {
      return new BattleResponse(
          battle.getId().value(),
          battle.getGuildId().value(),
          battle.getBattleStatus(),
          battle.getCurrentTurn(),
          battle.getNumOfTurns(),
          BossResponse.from(battle.getBoss()),
          battle.getBossRemainingHealth().remainingHealth().value());
    }
  }

  public record BossResponse(
      String name,
      int health,
      int strength,
      int defense,
      int experienceReward,
      int moneyReward,
      int penalty)
      implements QueryResponse {

    public static BossResponse from(BossEnemy boss) {
      return new BossResponse(
          boss.name(),
          boss.stats().health().value(),
          boss.stats().strength().value(),
          boss.stats().defense().value(),
          boss.experienceReward().amount(),
          boss.moneyReward().amount(),
          boss.penalty().amount());
    }
  }
}
