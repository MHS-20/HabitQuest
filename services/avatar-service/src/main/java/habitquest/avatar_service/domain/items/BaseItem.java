package habitquest.avatar_service.domain.items;

public record BaseItem(String name, String description) implements Item {
    public BaseItem {
        if (name == null || name.isBlank()) {
        throw new IllegalArgumentException("Item name cannot be null or blank");
        }

        if (description == null) {
        throw new IllegalArgumentException("Item description cannot be null");
        }
    }
}
