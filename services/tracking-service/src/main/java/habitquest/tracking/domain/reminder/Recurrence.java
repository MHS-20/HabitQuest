package habitquest.tracking.domain.reminder;

import common.ddd.ValueObject;

import java.time.LocalDate;

public interface Recurrence extends ValueObject {
    LocalDate nextAfter(LocalDate current);
}
