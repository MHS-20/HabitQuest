package habitquest.guild.infrastructure.dto;

import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.BattleStatus;

public record BattleResponse(
    String id,
    String guildId,
    BattleStatus status,
    int currentTurn,
    int numOfTurns,
    BossResponse boss,
    int bossRemainingHealth) {

  public static BattleResponse from(Battle battle) {
    return new BattleResponse(
        battle.getId(),
        battle.getGuildId(),
        battle.getBattleStatus(),
        battle.getCurrentTurn(),
        battle.getNumOfTurns(),
        BossResponse.from(battle.getBoss()),
        battle.getBossRemainingHealth().remainingHealth().value());
  }
}
