package habitquest.avatar.domain;

import static org.assertj.core.api.Assertions.*;

import habitquest.avatar.domain.avatar.Experience;
import habitquest.avatar.domain.avatar.Level;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Level")
class LevelTest {

  /** Helper: level 1, 0/100 XP. */
  private static Level levelOne() {
    return new Level(1, new Experience(0), new Experience(100));
  }

  @Nested
  @DisplayName("construction")
  class Construction {

    @Test
    @DisplayName("rejects level number below 1")
    void belowMinLevel() {
      assertThatThrownBy(() -> new Level(0, new Experience(0), new Experience(100)))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rejects null experienceRequired")
    void nullExperienceRequired() {
      assertThatThrownBy(() -> new Level(1, new Experience(0), null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("gainExperience — no level-up")
  class GainExperienceNoLevelUp {

    @Test
    @DisplayName("accumulates experience below the threshold")
    void accumulates() {
      Level level = levelOne().gainExperience(new Experience(50));
      assertThat(level.levelNumber()).isEqualTo(1);
      assertThat(level.currentExperience().amount()).isEqualTo(50);
    }

    @Test
    @DisplayName("reaches the threshold without crossing it — no level-up")
    void exactlyAtThreshold() {
      // 99 XP is still below 100 → no level-up
      Level level = levelOne().gainExperience(new Experience(99));
      assertThat(level.levelNumber()).isEqualTo(1);
      assertThat(level.currentExperience().amount()).isEqualTo(99);
    }
  }

  @Nested
  @DisplayName("gainExperience — level-up")
  class GainExperienceLevelUp {

    @Test
    @DisplayName("levels up when experience meets or exceeds the required threshold")
    void levelUp() {
      Level level = levelOne().gainExperience(new Experience(100));
      assertThat(level.levelNumber()).isEqualTo(2);
    }

    @Test
    @DisplayName("resets current experience to zero after levelling up")
    void resetsExperience() {
      Level level = levelOne().gainExperience(new Experience(100));
      assertThat(level.currentExperience().amount()).isZero();
    }

    @Test
    @DisplayName("doubles the required experience after levelling up")
    void doublesRequirement() {
      Level level = levelOne().gainExperience(new Experience(100));
      assertThat(level.experienceRequired().amount()).isEqualTo(200);
    }

    @Test
    @DisplayName("gaining more than required still produces a single level-up")
    void overshoot() {
      Level level = levelOne().gainExperience(new Experience(150));
      assertThat(level.levelNumber()).isEqualTo(2);
    }
  }

  @Nested
  @DisplayName("resetExperience")
  class ResetExperience {

    @Test
    @DisplayName("resets current XP to zero without changing level or requirement")
    void reset() {
      Level withXp = levelOne().gainExperience(new Experience(60));
      Level reset = withXp.resetExperience();

      assertThat(reset.levelNumber()).isEqualTo(1);
      assertThat(reset.currentExperience().amount()).isZero();
      assertThat(reset.experienceRequired().amount()).isEqualTo(100);
    }
  }
}
