package habitquest.tracking.application.service;

import habitquest.tracking.application.port.out.HabitLogger;
import habitquest.tracking.application.port.out.HabitNotifier;
import habitquest.tracking.domain.events.*;
import org.springframework.stereotype.Component;

@Component
public class HabitObserverImpl implements HabitObserver {

  private final HabitNotifier habitNotifier;
  private final HabitLogger log;

  public HabitObserverImpl(HabitNotifier habitNotifier, HabitLogger log) {
    this.habitNotifier = habitNotifier;
    this.log = log;
  }

  @Override
  public void notifyHabitEvent(HabitEvent event) {
    log.info(event, "Received habit event");
    switch (event) {
      case HabitDeleted d -> handleHabitDeleted(d);
      case HabitAttended a -> handleHabitAttended(a);
      case HabitNotAttended a -> handleHabitNotAttended(a);
      default -> {
        log.warn(event, "Unknown event type received");
        throw new IllegalArgumentException("Unknown event type: " + event.getClass());
      }
    }
  }

  private void handleHabitDeleted(HabitDeleted d) {
    log.info(d, "Handling HabitDeleted event");
    habitNotifier.notifyHabitDeleted(d);
  }

  private void handleHabitAttended(HabitAttended a) {
    log.info(a, "Handling HabitAttended event");
    habitNotifier.notifyHabitAttended(a);
  }

  private void handleHabitNotAttended(HabitNotAttended a) {
    log.info(a, "Handling HabitNotAttended event");
    habitNotifier.notifyHabitNotAttended(a);
  }
}
