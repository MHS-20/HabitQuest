package habitquest.tracking.domain.reminder;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Objects;

public record WeeklyRecurrence(DayOfWeek dayOfWeek) implements Recurrence {
  public WeeklyRecurrence {
    Objects.requireNonNull(dayOfWeek);
  }

  @Override
  public LocalDateTime nextAfter(LocalDateTime current) {
    LocalDateTime next = current.plusWeeks(1);
    return next.with(dayOfWeek);
  }
}
