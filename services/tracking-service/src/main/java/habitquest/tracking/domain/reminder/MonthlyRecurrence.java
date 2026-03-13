package habitquest.tracking.domain.reminder;

import java.time.LocalDateTime;

public record MonthlyRecurrence(Integer dayOfMonth) implements Recurrence {

  @Override
  public LocalDateTime nextAfter(LocalDateTime current) {
    LocalDateTime next = current.plusMonths(1);
    return next.withDayOfMonth(Math.min(dayOfMonth, next.toLocalDate().lengthOfMonth()));
  }
}
