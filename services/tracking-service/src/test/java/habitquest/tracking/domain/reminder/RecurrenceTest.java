package habitquest.tracking.domain.reminder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Recurrence strategies")
class RecurrenceTest {

  @Test
  @DisplayName("DailyRecurrence returns next day with same time")
  void dailyRecurrence() {
    DailyRecurrence recurrence = new DailyRecurrence();

    LocalDateTime next = recurrence.nextAfter(LocalDateTime.of(2026, 3, 19, 10, 15));

    assertThat(next).isEqualTo(LocalDateTime.of(2026, 3, 20, 10, 15));
  }

  @Test
  @DisplayName("WeeklyRecurrence aligns to configured day of week")
  void weeklyRecurrence() {
    WeeklyRecurrence recurrence = new WeeklyRecurrence(DayOfWeek.MONDAY);

    LocalDateTime next = recurrence.nextAfter(LocalDateTime.of(2026, 3, 19, 10, 15));

    assertThat(next).isEqualTo(LocalDateTime.of(2026, 3, 23, 10, 15));
  }

  @Test
  @DisplayName("WeeklyRecurrence rejects null day")
  void weeklyRecurrenceRejectsNullDay() {
    assertThatThrownBy(() -> new WeeklyRecurrence(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("MonthlyRecurrence clamps day to month length")
  void monthlyRecurrenceClampsDay() {
    MonthlyRecurrence recurrence = new MonthlyRecurrence(31);

    LocalDateTime next = recurrence.nextAfter(LocalDateTime.of(2026, 1, 30, 7, 45));

    assertThat(next).isEqualTo(LocalDateTime.of(2026, 2, 28, 7, 45));
  }
}
