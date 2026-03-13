package habitquest.tracking.domain.reminder;

import common.ddd.ValueObject;
import java.time.LocalDateTime;

public interface Recurrence extends ValueObject {
  LocalDateTime nextAfter(LocalDateTime current);
}
