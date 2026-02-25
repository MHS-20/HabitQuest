package habitquest.avatar_service.domain.items;

public record Weapon(BaseItem baseItem, int attackPower) implements Item {
    public Weapon {
        if (attackPower < 0) {
            throw new IllegalArgumentException("Attack power cannot be negative");
        }
    }

    @Override
    public String name() {
        return baseItem.name();
    }

    @Override
    public String description() {
        return baseItem.description();
    }
}
