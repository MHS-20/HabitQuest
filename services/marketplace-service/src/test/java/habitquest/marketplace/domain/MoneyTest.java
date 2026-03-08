package habitquest.marketplace.domain;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MoneyTest {

  // ── Construction ────────────────────────────────────────────────────────────

  @Test
  void shouldCreateMoneyWithZeroAmount() {
    Money money = new Money(0);
    assertThat(money.amount()).isZero();
  }

  @Test
  void shouldCreateMoneyWithPositiveAmount() {
    Money money = new Money(100);
    assertThat(money.amount()).isEqualTo(100);
  }

  @Test
  void shouldRejectNegativeAmount() {
    assertThatThrownBy(() -> new Money(-1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("negative");
  }

  @Test
  void shouldRejectNullAmount() {
    assertThatThrownBy(() -> new Money(null)).isInstanceOf(NullPointerException.class);
  }

  // ── Add ─────────────────────────────────────────────────────────────────────

  @Test
  void shouldAddTwoMoneyValues() {
    Money result = new Money(30).add(new Money(20));
    assertThat(result.amount()).isEqualTo(50);
  }

  @Test
  void shouldAddZeroWithoutChangingAmount() {
    Money result = new Money(100).add(new Money(0));
    assertThat(result.amount()).isEqualTo(100);
  }

  // ── Subtract ─────────────────────────────────────────────────────────────────

  @Test
  void shouldSubtractSmallerAmount() {
    Money result = new Money(100).subtract(new Money(40));
    assertThat(result.amount()).isEqualTo(60);
  }

  @Test
  void shouldSubtractEqualAmount() {
    Money result = new Money(50).subtract(new Money(50));
    assertThat(result.amount()).isZero();
  }

  @Test
  void shouldRejectSubtractingMoreThanAvailable() {
    assertThatThrownBy(() -> new Money(10).subtract(new Money(20)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Cannot subtract");
  }

  // ── Equality (record) ────────────────────────────────────────────────────────

  @Test
  void shouldBeEqualWhenAmountIsTheSame() {
    assertThat(new Money(42)).isEqualTo(new Money(42));
  }

  @Test
  void shouldNotBeEqualWhenAmountDiffers() {
    assertThat(new Money(10)).isNotEqualTo(new Money(11));
  }
}
