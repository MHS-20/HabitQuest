package habitquest.tracking.application.port.out;

import common.ddd.Id;
import common.hexagonal.OutBoundPort;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;
import java.time.LocalDate;

@OutBoundPort
public interface QuestClientPort {
  void recordHabitAttendance(
      String questId, Id<Avatar> avatarId, Id<Habit> habitId, LocalDate attendedOn);
}
