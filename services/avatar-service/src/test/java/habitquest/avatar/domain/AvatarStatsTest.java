package habitquest.avatar.domain;

import static org.assertj.core.api.Assertions.*;

import habitquest.avatar.domain.stats.AvatarStats;
import habitquest.avatar.domain.stats.BaseStat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("AvatarStats")
class AvatarStatsTest {

  private AvatarStats stats;

  @BeforeEach
  void setUp() {
    stats = new AvatarStats("stats-1", 10, 10, 10);
  }

  @Nested
  @DisplayName("initial state")
  class InitialState {

    @Test
    @DisplayName("all stats are initialised with the given values")
    void initialValues() {
      assertThat(stats.getStrength().value()).isEqualTo(10);
      assertThat(stats.getDefense().value()).isEqualTo(10);
      assertThat(stats.getIntelligence().value()).isEqualTo(10);
    }
  }

  @Nested
  @DisplayName("incrementStrength")
  class IncrementStrength {

    @Test
    @DisplayName("increases strength by one")
    void increments() {
      stats.incrementStrength();
      assertThat(stats.getStrength().value()).isEqualTo(11);
    }

    @Test
    @DisplayName("does not affect defense or intelligence")
    void isolatedEffect() {
      stats.incrementStrength();
      assertThat(stats.getDefense().value()).isEqualTo(10);
      assertThat(stats.getIntelligence().value()).isEqualTo(10);
    }
  }

  @Nested
  @DisplayName("incrementDefense")
  class IncrementDefense {

    @Test
    @DisplayName("increases defense by one")
    void increments() {
      stats.incrementDefense();
      assertThat(stats.getDefense().value()).isEqualTo(11);
    }

    @Test
    @DisplayName("does not affect strength or intelligence")
    void isolatedEffect() {
      stats.incrementDefense();
      assertThat(stats.getStrength().value()).isEqualTo(10);
      assertThat(stats.getIntelligence().value()).isEqualTo(10);
    }
  }

  @Nested
  @DisplayName("incrementIntelligence")
  class IncrementIntelligence {

    @Test
    @DisplayName("increases intelligence by one")
    void increments() {
      stats.incrementIntelligence();
      assertThat(stats.getIntelligence().value()).isEqualTo(11);
    }

    @Test
    @DisplayName("does not affect strength or defense")
    void isolatedEffect() {
      stats.incrementIntelligence();
      assertThat(stats.getStrength().value()).isEqualTo(10);
      assertThat(stats.getDefense().value()).isEqualTo(10);
    }
  }

  @Nested
  @DisplayName("BaseStat guards")
  class BaseStatGuards {

    @Test
    @DisplayName("BaseStat rejects negative values")
    void negativeBaseStat() {
      assertThatThrownBy(() -> new BaseStat(-1)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("BaseStat.increment returns new instance with value + 1")
    void baseStatIncrement() {
      BaseStat base = new BaseStat(5);
      assertThat(base.increment().value()).isEqualTo(6);
      // immutable — original unchanged
      assertThat(base.value()).isEqualTo(5);
    }
  }
}
