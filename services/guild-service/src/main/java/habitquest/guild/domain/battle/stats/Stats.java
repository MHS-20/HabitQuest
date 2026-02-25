package habitquest.guild.domain.battle.stats;

import common.ddd.Aggregate;

public class Stats implements Aggregate<String> {

  private String id;

  public Stats(String id) {
    this.id = id;
  }

  @Override
  public String getId() {
    return this.id;
  }
}
