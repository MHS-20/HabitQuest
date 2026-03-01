package habitquest.guild.domain.battle.stats;

public record Defense(BaseStat stat) implements Stat {

  public Defense(Integer value) {
    this(new BaseStat(value));
  }

  @Override
  public Integer value() {
    return stat.value();
  }
}
