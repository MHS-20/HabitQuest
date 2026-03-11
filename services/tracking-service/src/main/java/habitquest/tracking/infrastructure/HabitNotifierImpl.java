package habitquest.tracking.infrastructure;

import common.hexagonal.Adapter;
import habitquest.tracking.application.HabitNotifier;
import habitquest.tracking.domain.events.HabitAttended;
import habitquest.tracking.domain.events.HabitDeleted;
import habitquest.tracking.domain.events.HabitNotAttended;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

@Adapter
@Component
public class HabitNotifierImpl implements HabitNotifier {

  private static final Logger LOG = LoggerFactory.getLogger(HabitNotifierImpl.class);

  static final String HABIT_DELETED_BINDING = "habit-deleted-out-0";
  static final String HABIT_ATTENDED_BINDING = "habit-attended-out-0";
  static final String HABIT_NOT_ATTENDED_BINDING = "habit-not-attended-out-0";

  private final StreamBridge streamBridge;

  public HabitNotifierImpl(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  @Override
  public void notifyHabitDeleted(HabitDeleted event) {
    HabitDeletedMessage message = new HabitDeletedMessage(event.habitId(), Instant.now());

    LOG.info("Publishing HabitDeleted event: habitId={}", message.habitId());
    boolean sent = streamBridge.send(HABIT_DELETED_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish HabitDeleted event for habitId {}", message.habitId());
    }
  }

  @Override
  public void notifyHabitAttended(HabitAttended event) {
    HabitAttendedMessage message =
        new HabitAttendedMessage(event.habit().getId(), event.habit().getAvatarId(), Instant.now());

    LOG.info(
        "Publishing HabitAttended event: habitId={}, avatarId={}",
        message.habitId(),
        message.avatarId());
    boolean sent = streamBridge.send(HABIT_ATTENDED_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish HabitAttended event for habitId {}", message.habitId());
    }
  }

  @Override
  public void notifyHabitNotAttended(HabitNotAttended event) {
    HabitNotAttendedMessage message =
        new HabitNotAttendedMessage(
            event.habit().getId(), event.habit().getAvatarId(), Instant.now());

    LOG.info(
        "Publishing HabitNotAttended event: habitId={}, avatarId={}",
        message.habitId(),
        message.avatarId());
    boolean sent = streamBridge.send(HABIT_NOT_ATTENDED_BINDING, message);
    if (!sent) {
      LOG.error("Failed to publish HabitNotAttended event for habitId {}", message.habitId());
    }
  }

  public record HabitDeletedMessage(String habitId, Instant occurredOn) {}

  public record HabitAttendedMessage(String habitId, String avatarId, Instant occurredOn) {}

  public record HabitNotAttendedMessage(String habitId, String avatarId, Instant occurredOn) {}
}
