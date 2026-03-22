package habitquest.tracking.application;

import common.ddd.Id;
import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.events.HabitHistoryEvent;
import java.util.List;

@OutBoundPort
public interface HabitHistoryRepository extends Repository {
  void append(HabitHistoryEvent event);

  List<HabitHistoryEvent> findByHabitId(Id<Habit> habitId);
}
