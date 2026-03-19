package habitquest.tracking.infrastructure;

import common.hexagonal.Adapter;
import habitquest.tracking.application.HabitHistoryRepository;
import habitquest.tracking.domain.events.HabitHistoryEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Adapter
@Repository
public class InMemoryHabitHistoryRepository implements HabitHistoryRepository {

  private final Map<String, List<HabitHistoryEvent>> store = new ConcurrentHashMap<>();

  @Override
  public void append(HabitHistoryEvent event) {
    store.computeIfAbsent(event.habitId(), unused -> new ArrayList<>()).add(event);
  }

  @Override
  public List<HabitHistoryEvent> findByHabitId(String habitId) {
    return List.copyOf(store.getOrDefault(habitId, List.of()));
  }
}
