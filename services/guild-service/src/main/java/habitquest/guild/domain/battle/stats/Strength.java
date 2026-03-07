package habitquest.guild.domain.battle.stats;

public record Strength(BaseStat stat) implements Stat {

  public Strength(Integer value) {
    this(new BaseStat(value));
  }

  @Override
  public Integer value() {
    return stat.value();
  }
}
