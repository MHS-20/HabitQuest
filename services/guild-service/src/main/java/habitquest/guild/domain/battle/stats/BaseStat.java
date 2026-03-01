package habitquest.guild.domain.battle.stats;

public record BaseStat(Integer value) implements Stat {
  public BaseStat {
    if (value < 0) {
      throw new IllegalArgumentException("Stat value cannot be negative");
    }
  }
}
