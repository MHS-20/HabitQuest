package habitquest.avatar.domain;

import static org.assertj.core.api.Assertions.*;

import habitquest.avatar.domain.avatar.Money;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Money")
class MoneyTest {

  @Nested
  @DisplayName("construction")
  class Construction {

    @Test
    @DisplayName("creates with zero amount")
    void zero() {
      Assertions.assertThat(new Money(0).amount()).isZero();
    }

    @Test
    @DisplayName("creates with positive amount")
    void positive() {
      assertThat(new Money(42).amount()).isEqualTo(42);
    }

    @Test
    @DisplayName("rejects negative amount")
    void negative() {
      assertThatThrownBy(() -> new Money(-1)).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("add")
  class Add {

    @Test
    @DisplayName("returns sum of both amounts")
    void addsAmounts() {
      assertThat(new Money(10).add(new Money(5)).amount()).isEqualTo(15);
    }

    @Test
    @DisplayName("adding zero leaves amount unchanged")
    void addZero() {
      assertThat(new Money(10).add(new Money(0)).amount()).isEqualTo(10);
    }
  }

  @Nested
  @DisplayName("subtract")
  class Subtract {

    @Test
    @DisplayName("returns difference when funds are sufficient")
    void sufficientFunds() {
      assertThat(new Money(10).subtract(new Money(3)).amount()).isEqualTo(7);
    }

    @Test
    @DisplayName("returns zero when amounts are equal")
    void exactAmount() {
      assertThat(new Money(10).subtract(new Money(10)).amount()).isZero();
    }

    @Test
    @DisplayName("throws when subtracting more than available")
    void insufficientFunds() {
      assertThatThrownBy(() -> new Money(5).subtract(new Money(10)))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("isEnough")
  class IsEnough {

    @Test
    @DisplayName("returns true when amount is equal")
    void equal() {
      assertThat(new Money(10).isEnough(new Money(10))).isTrue();
    }

    @Test
    @DisplayName("returns true when amount is greater")
    void greater() {
      assertThat(new Money(20).isEnough(new Money(10))).isTrue();
    }

    @Test
    @DisplayName("returns false when amount is less")
    void less() {
      assertThat(new Money(5).isEnough(new Money(10))).isFalse();
    }
  }
}
