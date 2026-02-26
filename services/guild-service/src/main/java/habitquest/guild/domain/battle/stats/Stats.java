package habitquest.guild.domain.battle.stats;

import common.ddd.Aggregate;
import java.util.Objects;

public class Stats implements Aggregate<String> {

  private String id;

  public Stats(String id) {
    this.id = Objects.requireNonNull(id);
  }

  @Override
  public String getId() {
    return this.id;
  }
}
