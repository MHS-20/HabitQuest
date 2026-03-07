package habitquest.guild.domain.battle.stats;

public record Health(BaseStat stat) implements Stat {
  public Health(Integer value) {
    this(new BaseStat(value));
  }

  @Override
  public Integer value() {
    return stat.value();
  }
}
