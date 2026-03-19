package habitquest.notification.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface EventConsumer {
  default Logger logger() {
    return LoggerFactory.getLogger(getClass());
  }
}
