package habitquest.avatar_service.domain.items;

public record HealthPotion(BaseItem baseItem, int healingPower) implements Item {
    public HealthPotion {
        if (healingPower < 0) {
            throw new IllegalArgumentException("Healing power cannot be negative");
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
