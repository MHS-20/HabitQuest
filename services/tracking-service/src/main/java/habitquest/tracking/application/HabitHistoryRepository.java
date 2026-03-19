package habitquest.tracking.application;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.tracking.domain.events.HabitHistoryEvent;
import java.util.List;

@OutBoundPort
public interface HabitHistoryRepository extends Repository {
  void append(HabitHistoryEvent event);

  List<HabitHistoryEvent> findByHabitId(String habitId);
}
