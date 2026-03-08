package habitquest.avatar.domain;

import static org.assertj.core.api.Assertions.*;

import habitquest.avatar.domain.avatar.Experience;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Experience")
class ExperienceTest {

  @Nested
  @DisplayName("construction")
  class Construction {

    @Test
    @DisplayName("creates with zero amount")
    void zero() {
      Assertions.assertThat(new Experience(0).amount()).isZero();
    }

    @Test
    @DisplayName("rejects null amount")
    void nullAmount() {
      assertThatThrownBy(() -> new Experience(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("rejects negative amount")
    void negative() {
      assertThatThrownBy(() -> new Experience(-1)).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("add")
  class Add {

    @Test
    @DisplayName("sums amounts correctly")
    void addsAmounts() {
      assertThat(new Experience(30).add(new Experience(20)).amount()).isEqualTo(50);
    }

    @Test
    @DisplayName("adding zero leaves amount unchanged")
    void addZero() {
      assertThat(new Experience(50).add(new Experience(0)).amount()).isEqualTo(50);
    }
  }

  @Nested
  @DisplayName("reset")
  class Reset {

    @Test
    @DisplayName("resetExperience always returns zero")
    void reset() {
      assertThat(new Experience(99).resetExperience().amount()).isZero();
    }
  }

  @Nested
  @DisplayName("comparisons")
  class Comparisons {

    @Test
    @DisplayName("isAtLeast returns true for equal value")
    void atLeastEqual() {
      assertThat(new Experience(10).isAtLeast(new Experience(10))).isTrue();
    }

    @Test
    @DisplayName("isAtLeast returns true for greater value")
    void atLeastGreater() {
      assertThat(new Experience(15).isAtLeast(new Experience(10))).isTrue();
    }

    @Test
    @DisplayName("isAtLeast returns false for lesser value")
    void atLeastLess() {
      assertThat(new Experience(5).isAtLeast(new Experience(10))).isFalse();
    }

    @Test
    @DisplayName("isGreaterThan returns true when strictly greater")
    void greaterThan() {
      assertThat(new Experience(11).isGreaterThan(new Experience(10))).isTrue();
    }

    @Test
    @DisplayName("isGreaterThan returns false for equal")
    void notGreaterThanEqual() {
      assertThat(new Experience(10).isGreaterThan(new Experience(10))).isFalse();
    }

    @Test
    @DisplayName("isLessThan returns true when strictly less")
    void lessThan() {
      assertThat(new Experience(9).isLessThan(new Experience(10))).isTrue();
    }
  }
}
