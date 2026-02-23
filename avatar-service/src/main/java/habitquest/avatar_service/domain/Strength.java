package habitquest.avatar_service.domain;

public record Strength(BaseStat stat) implements PlayerStat {

    public Strength(int value) {
        this(new BaseStat(value));
    }

    @Override
    public Strength increment() {
        return new Strength(stat.increment());
    }

    @Override
    public int value() {
        return stat.value();
    }
}