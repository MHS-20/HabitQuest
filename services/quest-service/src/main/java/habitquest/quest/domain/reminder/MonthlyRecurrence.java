package habitquest.quest.domain.reminder;

import java.time.LocalDate;

public record MonthlyRecurrence(Integer dayOfMonth) implements Recurrence {

  @Override
  public LocalDate nextAfter(LocalDate current) {
    LocalDate next = current.plusMonths(1);
    return next.withDayOfMonth(Math.min(dayOfMonth, next.lengthOfMonth()));
  }
}
