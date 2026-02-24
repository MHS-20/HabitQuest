package habitquest.avatar_service.domain;

import java.util.Objects;

public record Intelligence(BaseStat stat) implements PlayerStat {
  public Intelligence {
    Objects.requireNonNull(stat);
  }

  public Intelligence(int value) {
    this(new BaseStat(value));
  }

  @Override
  public Intelligence increment() {
    return new Intelligence(stat.increment());
  }

  @Override
  public int value() {
    return stat.value();
  }
}
