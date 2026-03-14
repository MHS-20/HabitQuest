package habitquest.guild.infrastructure.dto;

import habitquest.guild.domain.battle.boss.BossEnemy;

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
