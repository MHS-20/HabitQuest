package habitquest.tracking.domain.reminder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

public record WeeklyRecurrence(DayOfWeek dayOfWeek) implements Recurrence {
    public WeeklyRecurrence {
        Objects.requireNonNull(dayOfWeek);
    }

    @Override
    public LocalDate nextAfter(LocalDate current) {
        LocalDate next = current.plusWeeks(1);
        return next.with(dayOfWeek);
    }
}