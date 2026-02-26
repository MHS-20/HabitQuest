package habitquest.tracking.domain.reminder;

import java.time.LocalDate;

public record DailyRecurrence() implements Recurrence {

    @Override
    public LocalDate nextAfter(LocalDate current) {
        return current.plusDays(1);
    }
}
