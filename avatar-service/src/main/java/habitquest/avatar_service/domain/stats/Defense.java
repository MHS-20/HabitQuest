package habitquest.avatar_service.domain.stats;

public record Defense(BaseStat stat) implements AvatarStat {

  public Defense(int value) {
    this(new BaseStat(value));
  }

  @Override
  public Defense increment() {
    return new Defense(stat.increment());
  }

  @Override
  public Integer value() {
    return stat.value();
  }
}
