package habitquest.tracking.domain.reminder;

import java.time.LocalDateTime;

public record DailyRecurrence() implements Recurrence {

  @Override
  public LocalDateTime nextAfter(LocalDateTime current) {
    return current.plusDays(1);
  }
}
