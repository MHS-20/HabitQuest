package habitquest.guild.domain.battle.stats;

public record Strength(BaseStat stat) implements Stat {

  public Strength(Integer value) {
    this(new BaseStat(value));
  }

  @Override
  public Strength increment() {
    return new Strength(stat.increment());
  }

  @Override
  public Integer value() {
    return stat.value();
  }
}
