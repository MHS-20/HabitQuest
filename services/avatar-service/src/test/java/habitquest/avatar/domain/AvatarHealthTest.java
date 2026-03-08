package habitquest.avatar.domain;

import static org.assertj.core.api.Assertions.*;

import habitquest.avatar.domain.avatar.AvatarHealth;
import habitquest.avatar.domain.avatar.Health;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AvatarHealth")
class AvatarHealthTest {

  private static final Health MAX = new Health(100);

  @Nested
  @DisplayName("construction")
  class Construction {

    @Test
    @DisplayName("accepts full health")
    void full() {
      assertThatNoException().isThrownBy(() -> new AvatarHealth(MAX, MAX));
    }

    @Test
    @DisplayName("accepts zero current health")
    void currentZero() {
      assertThatNoException().isThrownBy(() -> new AvatarHealth(new Health(0), MAX));
    }

    @Test
    @DisplayName("rejects max health of zero")
    void zeroMax() {
      assertThatThrownBy(() -> new AvatarHealth(new Health(0), new Health(0)))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rejects current health above max")
    void currentAboveMax() {
      assertThatThrownBy(() -> new AvatarHealth(new Health(150), new Health(100)))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("damage")
  class Damage {

    @Test
    @DisplayName("reduces current health by damage amount")
    void reducesCurrent() {
      AvatarHealth health = new AvatarHealth(MAX, MAX).damage(new Health(30));
      assertThat(health.current().value()).isEqualTo(70);
    }

    @Test
    @DisplayName("clamps current health to zero — does not go negative")
    void clampToZero() {
      AvatarHealth health = new AvatarHealth(MAX, MAX).damage(new Health(200));
      assertThat(health.current().value()).isZero();
    }

    @Test
    @DisplayName("does not change max health when damaged")
    void maxUnchanged() {
      AvatarHealth health = new AvatarHealth(MAX, MAX).damage(new Health(40));
      assertThat(health.max().value()).isEqualTo(100);
    }
  }

  @Nested
  @DisplayName("heal")
  class Heal {

    @Test
    @DisplayName("increases current health by heal amount")
    void increasesCurrent() {
      AvatarHealth health = new AvatarHealth(new Health(50), MAX).heal(new Health(20));
      assertThat(health.current().value()).isEqualTo(70);
    }

    @Test
    @DisplayName("clamps current health to max — cannot over-heal")
    void clampToMax() {
      AvatarHealth health = new AvatarHealth(new Health(90), MAX).heal(new Health(50));
      assertThat(health.current().value()).isEqualTo(100);
    }
  }

  @Nested
  @DisplayName("isDead")
  class IsDead {

    @Test
    @DisplayName("returns true when current health is zero")
    void dead() {
      assertThat(new AvatarHealth(new Health(0), MAX).isDead()).isTrue();
    }

    @Test
    @DisplayName("returns false when current health is positive")
    void alive() {
      assertThat(new AvatarHealth(new Health(1), MAX).isDead()).isFalse();
    }
  }

  @Nested
  @DisplayName("resetHealth")
  class ResetHealth {

    @Test
    @DisplayName("restores current health to max")
    void resetsToMax() {
      AvatarHealth health = new AvatarHealth(new Health(10), MAX).resetHealth();
      assertThat(health.current().value()).isEqualTo(100);
    }
  }

  @Nested
  @DisplayName("increaseMax")
  class IncreaseMax {

    @Test
    @DisplayName("increases both current and max health by the given amount")
    void increasesBoth() {
      AvatarHealth health = new AvatarHealth(MAX, MAX).increaseMax(new Health(10));
      assertThat(health.max().value()).isEqualTo(110);
      assertThat(health.current().value()).isEqualTo(110);
    }
  }
}
