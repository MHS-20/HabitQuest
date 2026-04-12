package habitquest.quest.domain.reminder;

import common.ddd.ValueObject;
import java.time.LocalDate;

public sealed interface Recurrence extends ValueObject
    permits DailyRecurrence, WeeklyRecurrence, MonthlyRecurrence {
  LocalDate nextAfter(LocalDate current);
}
