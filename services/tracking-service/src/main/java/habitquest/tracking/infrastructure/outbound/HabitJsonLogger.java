package habitquest.tracking.infrastructure.outbound;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.hexagonal.Adapter;
import habitquest.tracking.application.port.out.HabitLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class HabitJsonLogger implements HabitLogger {

  private final Logger log = LoggerFactory.getLogger(HabitJsonLogger.class);
  private final ObjectMapper objectMapper;

  public HabitJsonLogger(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void info(Object domainObject, String message) {
    log.info("[{}] {} | {}", className(domainObject), message, serialize(domainObject));
  }

  @Override
  public void warn(Object domainObject, String message) {
    log.warn("[{}] {} | {}", className(domainObject), message, serialize(domainObject));
  }

  @Override
  public void error(Object domainObject, String message, Throwable cause) {
    log.error("[{}] {} | {}", className(domainObject), message, serialize(domainObject), cause);
  }

  private String serialize(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (JsonProcessingException e) {
      return obj == null ? "null" : obj.toString();
    }
  }

  private String className(Object obj) {
    return obj == null ? "null" : obj.getClass().getSimpleName();
  }
}
