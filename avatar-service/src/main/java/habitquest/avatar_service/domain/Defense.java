package habitquest.avatar_service.domain;

public record Defense(BaseStat stat) implements PlayerStat {

    public Defense(int value) {
        this(new BaseStat(value));
    }

    @Override
    public Defense increment() {
        return new Defense(stat.increment());
    }

    @Override
    public int value() {
        return stat.value();
    }
}