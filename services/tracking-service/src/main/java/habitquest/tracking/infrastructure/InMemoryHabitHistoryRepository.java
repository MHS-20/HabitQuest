package habitquest.tracking.infrastructure;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.tracking.application.HabitHistoryRepository;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.events.HabitHistoryEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Adapter
@Repository
public class InMemoryHabitHistoryRepository implements HabitHistoryRepository {

  private final Map<Id<Habit>, List<HabitHistoryEvent>> store = new ConcurrentHashMap<>();

  @Override
  public void append(HabitHistoryEvent event) {
    store.computeIfAbsent(event.habitId(), unused -> new ArrayList<>()).add(event);
  }

  @Override
  public List<HabitHistoryEvent> findByHabitId(Id<Habit> habitId) {
    return List.copyOf(store.getOrDefault(habitId, List.of()));
  }
}
