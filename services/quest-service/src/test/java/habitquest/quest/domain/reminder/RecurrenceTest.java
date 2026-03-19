package habitquest.quest.domain.reminder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Recurrence domain")
class RecurrenceTest {

  private static final LocalDate BASE_DATE = LocalDate.of(2026, 3, 19);

  @Test
  @DisplayName("DailyRecurrence returns next day")
  void dailyRecurrenceMovesOneDay() {
    DailyRecurrence recurrence = new DailyRecurrence();

    LocalDate next = recurrence.nextAfter(BASE_DATE);

    assertThat(next).isEqualTo(LocalDate.of(2026, 3, 20));
  }

  @Test
  @DisplayName("WeeklyRecurrence aligns to configured day")
  void weeklyRecurrenceAlignsToDayOfWeek() {
    WeeklyRecurrence recurrence = new WeeklyRecurrence(DayOfWeek.MONDAY);

    LocalDate next = recurrence.nextAfter(BASE_DATE);

    assertThat(next).isEqualTo(LocalDate.of(2026, 3, 23));
  }

  @Test
  @DisplayName("WeeklyRecurrence rejects null day")
  void weeklyRecurrenceRejectsNullDayOfWeek() {
    assertThatThrownBy(() -> new WeeklyRecurrence(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("MonthlyRecurrence clamps to month length")
  void monthlyRecurrenceClampsDayOfMonth() {
    MonthlyRecurrence recurrence = new MonthlyRecurrence(31);

    LocalDate next = recurrence.nextAfter(LocalDate.of(2026, 1, 30));

    assertThat(next).isEqualTo(LocalDate.of(2026, 2, 28));
  }
}
