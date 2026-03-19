package habitquest.tracking.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tag value object")
class TagTest {

  @Test
  @DisplayName("creates tag with valid name")
  void createsWithValidName() {
    Tag tag = new Tag("health");

    assertThat(tag.name()).isEqualTo("health");
  }

  @Test
  @DisplayName("rejects null name")
  void rejectsNullName() {
    assertThatThrownBy(() -> new Tag(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("rejects blank name")
  void rejectsBlankName() {
    assertThatThrownBy(() -> new Tag("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Tag name cannot be blank");
  }
}
