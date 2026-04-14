package habitquest.guild.infrastructure.dto;

import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleOutcome;
import habitquest.guild.domain.battle.boss.BossEnemy;

public class BattleResponsesDto {
  public record BattleCreatedResponse(String id) {}

  public record BossHealthResponse(int remainingHealth) {}

  public record TurnResponse(Integer turn) {}

  public record InProgressResponse(boolean inProgress) {}

  public record BattleStatusResponse(BattleOutcome status, boolean isOver, boolean isWon) {}

  public record ErrorResponse(String message) {}

  public record BattleOutcomeLog(String battleId, String outcome, int primary, int secondary) {}

  public record BattleResponse(
      String id,
      String guildId,
      BattleOutcome status,
      int currentTurn,
      int numOfTurns,
      BossResponse boss,
      int bossRemainingHealth) {

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
      int penalty) {

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
