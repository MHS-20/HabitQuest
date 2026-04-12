package habitquest.tracking.domain.reminder;

import common.ddd.ValueObject;
import java.time.LocalDateTime;

public sealed interface Recurrence extends ValueObject
    permits DailyRecurrence, WeeklyRecurrence, MonthlyRecurrence {
  LocalDateTime nextAfter(LocalDateTime current);
}
