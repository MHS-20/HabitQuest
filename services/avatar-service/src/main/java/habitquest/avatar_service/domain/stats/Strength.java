package habitquest.avatar_service.domain.stats;

public record Strength(BaseStat stat) implements AvatarStat {

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
