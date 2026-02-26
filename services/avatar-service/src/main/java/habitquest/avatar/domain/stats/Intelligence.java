package habitquest.avatar.domain.stats;

import java.util.Objects;

public record Intelligence(BaseStat stat) implements AvatarStat {
  public Intelligence {
    Objects.requireNonNull(stat);
  }

  public Intelligence(Integer value) {
    this(new BaseStat(value));
  }

  @Override
  public Intelligence increment() {
    return new Intelligence(stat.increment());
  }

  @Override
  public Integer value() {
    return stat.value();
  }
}
