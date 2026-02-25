package habitquest.avatar_service.domain.items;

public record ManaPotion(BaseItem baseItem, int restoringPower) implements Item {
    public ManaPotion {
        if (restoringPower < 0) {
            throw new IllegalArgumentException("Mana power cannot be negative");
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
