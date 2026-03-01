package habitquest.guild.domain.battle.stats;

import common.ddd.Aggregate;

public record Stats(String id, Health health, Strength strength, Defense defense)
    implements Aggregate<String> {

  @Override
  public String getId() {
    return this.id;
  }
}
