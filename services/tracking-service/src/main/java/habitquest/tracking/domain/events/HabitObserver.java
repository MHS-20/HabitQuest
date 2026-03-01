package habitquest.tracking.domain.events;

public interface HabitObserver {
  void notifyHabitEvent(HabitEvent event);
}
