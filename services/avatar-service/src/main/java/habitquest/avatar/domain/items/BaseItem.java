package habitquest.avatar.domain.items;

import java.util.Objects;

public record BaseItem(String name, String description, int power) {
  public BaseItem {
    Objects.requireNonNull(name);
    Objects.requireNonNull(description);
    if (name.isBlank()) {
      throw new IllegalArgumentException("Item name cannot be null or blank");
    }
    if (power < 0) {
      throw new IllegalArgumentException("Power cannot be negative");
    }
  }
}
