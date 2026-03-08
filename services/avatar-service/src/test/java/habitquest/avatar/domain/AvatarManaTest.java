package habitquest.avatar.domain;

import static org.assertj.core.api.Assertions.*;

import habitquest.avatar.domain.avatar.AvatarMana;
import habitquest.avatar.domain.avatar.Mana;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AvatarMana")
class AvatarManaTest {

  private static final Mana MAX = new Mana(50);

  @Nested
  @DisplayName("construction")
  class Construction {

    @Test
    @DisplayName("rejects negative amount")
    void negativeAmount() {
      // Health record guards Mana at construction
      assertThatThrownBy(() -> new Mana(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rejects null amount")
    void nullAmount() {
      assertThatThrownBy(() -> new AvatarMana(null, MAX)).isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("add")
  class Add {

    @Test
    @DisplayName("restores mana up to the given amount")
    void restores() {
      AvatarMana mana = new AvatarMana(new Mana(20), MAX).add(new Mana(10));
      assertThat(mana.amount().value()).isEqualTo(30);
    }

    @Test
    @DisplayName("clamps to max — cannot exceed max mana")
    void clampToMax() {
      AvatarMana mana = new AvatarMana(new Mana(45), MAX).add(new Mana(20));
      assertThat(mana.amount().value()).isEqualTo(50);
    }

    @Test
    @DisplayName("max is unchanged after add")
    void maxUnchanged() {
      AvatarMana mana = new AvatarMana(new Mana(10), MAX).add(new Mana(5));
      assertThat(mana.max().value()).isEqualTo(50);
    }
  }

  @Nested
  @DisplayName("subtract")
  class Subtract {

    @Test
    @DisplayName("reduces mana by given amount")
    void reduces() {
      AvatarMana mana = new AvatarMana(MAX, MAX).subtract(new Mana(10));
      assertThat(mana.amount().value()).isEqualTo(40);
    }

    @Test
    @DisplayName("throws when spending more mana than available")
    void insufficientMana() {
      assertThatThrownBy(() -> new AvatarMana(new Mana(5), MAX).subtract(new Mana(10)))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("resetMana")
  class ResetMana {

    @Test
    @DisplayName("restores mana to max")
    void resetsToMax() {
      AvatarMana mana = new AvatarMana(new Mana(10), MAX).resetMana();
      assertThat(mana.amount().value()).isEqualTo(50);
    }
  }

  @Nested
  @DisplayName("increaseMax")
  class IncreaseMax {

    @Test
    @DisplayName("increases both current and max mana by given amount")
    void increasesBoth() {
      AvatarMana mana = new AvatarMana(MAX, MAX).increaseMax(new Mana(5));
      assertThat(mana.max().value()).isEqualTo(55);
      assertThat(mana.amount().value()).isEqualTo(55);
    }
  }
}
