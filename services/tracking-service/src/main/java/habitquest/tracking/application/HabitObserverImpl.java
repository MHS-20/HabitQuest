package habitquest.tracking.application;

import habitquest.tracking.domain.events.*;

public class HabitObserverImpl implements HabitObserver {

  private final HabitNotifier habitNotifier;

  public HabitObserverImpl(HabitNotifier habitNotifier) {
    this.habitNotifier = habitNotifier;
  }

  @Override
  public void notifyHabitEvent(HabitEvent event) {
    switch (event) {
      case HabitDeleted d -> handleHabitDeleted(d);
      case HabitAttended a -> handleHabitAttended(a);
      case HabitNotAttended a -> handleHabitNotAttended(a);
      default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }
  }

  private void handleHabitDeleted(HabitDeleted d) {
    habitNotifier.notifyHabitDeleted(d);
  }

  private void handleHabitAttended(HabitAttended a) {
    habitNotifier.notifyHabitAttended(a);
  }

  private void handleHabitNotAttended(HabitNotAttended a) {
    habitNotifier.notifyHabitNotAttended(a);
  }
}
