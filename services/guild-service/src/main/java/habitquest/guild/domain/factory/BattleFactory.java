package habitquest.guild.domain.factory;

import common.ddd.Factory;
import common.ddd.Id;
import habitquest.guild.domain.battle.Battle;
import habitquest.guild.domain.battle.boss.BossType;
import habitquest.guild.domain.guild.Guild;
import habitquest.guild.domain.guild.GuildMember;
import java.util.List;

public class BattleFactory implements Factory {

  private final IdGenerator idGenerator;

  public BattleFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Battle create(
      Id<Guild> guildId, BossType bossType, Integer numOfTurns, List<Id<GuildMember>> members) {
    return new Battle(new Id<Battle>(idGenerator.nextId()), guildId, bossType, numOfTurns, members);
  }
}
