package habitquest.avatar_service.domain.items;

public record Armor(BaseItem baseItem, int defensePower) implements Item {
    public Armor {
        if (defensePower < 0) {
            throw new IllegalArgumentException("Defense power cannot be negative");
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

    public Integer getValue() {
        return defensePower;
    }

}