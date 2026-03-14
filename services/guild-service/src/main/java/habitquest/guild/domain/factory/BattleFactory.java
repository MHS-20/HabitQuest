package habitquest.guild.domain.factory;

import common.ddd.Factory;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.boss.BossType;
import org.springframework.stereotype.Component;

@Component
public class BattleFactory implements Factory {

  private final IdGenerator idGenerator;

  public BattleFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Battle create(String guildId, BossType bossType, Integer numOfTurns) {
    String battleId = idGenerator.nextId();
    return new Battle(battleId, guildId, bossType, numOfTurns);
  }
}
