package habitquest.tracking.application;

import common.hexagonal.OutBoundPort;
import habitquest.tracking.domain.events.*;

@OutBoundPort
public interface HabitNotifier {
  void notifyHabitDeleted(HabitDeleted d);

  void notifyHabitAttended(HabitAttended a);

  void notifyHabitNotAttended(HabitNotAttended a);
}
