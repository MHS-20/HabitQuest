package habitquest.quest.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Tag value object")
class TagTest {

  private static final String VALID_TAG = "health";
  private static final String BLANK_TAG = "   ";
  private static final String BLANK_MESSAGE = "Tag name cannot be blank";

  @Test
  @DisplayName("creates tag with valid name")
  void createsWithValidName() {
    Tag tag = new Tag(VALID_TAG);

    assertThat(tag.name()).isEqualTo(VALID_TAG);
  }

  @Test
  @DisplayName("rejects null name")
  void rejectsNullName() {
    assertThatThrownBy(() -> new Tag(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("rejects blank name")
  void rejectsBlankName() {
    assertThatThrownBy(() -> new Tag(BLANK_TAG))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(BLANK_MESSAGE);
  }
}
