package habitquest.tracking.infrastructure;

import common.hexagonal.Adapter;
import habitquest.tracking.application.HabitLogger;
import habitquest.tracking.application.HabitNotifier;
import habitquest.tracking.domain.events.HabitAttended;
import habitquest.tracking.domain.events.HabitDeleted;
import habitquest.tracking.domain.events.HabitNotAttended;
import java.time.Instant;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class HabitNotifierImpl implements HabitNotifier {

  static final String HABIT_DELETED_BINDING = "habit.deleted";
  static final String HABIT_ATTENDED_BINDING = "habit.attended";
  static final String HABIT_NOT_ATTENDED_BINDING = "habit.not-attended";

  private final StreamBridge streamBridge;
  private final HabitLogger log;

  public HabitNotifierImpl(StreamBridge streamBridge, HabitLogger log) {
    this.streamBridge = streamBridge;
    this.log = log;
  }

  @Override
  public void notifyHabitDeleted(HabitDeleted event) {
    HabitDeletedMessage message =
        new HabitDeletedMessage(event.habitId().value(), event.avatarId().value(), Instant.now());

    log.info(message, "Publishing HabitDeleted event");
    boolean sent = streamBridge.send(HABIT_DELETED_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish HabitDeleted event", null);
    }
  }

  @Override
  public void notifyHabitAttended(HabitAttended event) {
    HabitAttendedMessage message =
        new HabitAttendedMessage(
            event.habit().getId().value(), event.avatarId().value(), Instant.now());

    log.info(message, "Publishing HabitAttended event");
    boolean sent = streamBridge.send(HABIT_ATTENDED_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish HabitAttended event", null);
    }
  }

  @Override
  public void notifyHabitNotAttended(HabitNotAttended event) {
    HabitNotAttendedMessage message =
        new HabitNotAttendedMessage(
            event.habit().getId().value(), event.avatarId().value(), Instant.now());

    log.info(message, "Publishing HabitNotAttended event");
    boolean sent = streamBridge.send(HABIT_NOT_ATTENDED_BINDING, message);
    if (!sent) {
      log.error(message, "Failed to publish HabitNotAttended event", null);
    }
  }

  public record HabitDeletedMessage(String habitId, String avatarId, Instant occurredOn) {}

  public record HabitAttendedMessage(String habitId, String avatarId, Instant occurredOn) {}

  public record HabitNotAttendedMessage(String habitId, String avatarId, Instant occurredOn) {}
}
