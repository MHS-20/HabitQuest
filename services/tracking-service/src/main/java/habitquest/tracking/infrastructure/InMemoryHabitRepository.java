package habitquest.tracking.infrastructure;

import common.hexagonal.Adapter;
import habitquest.tracking.application.HabitNotFoundException;
import habitquest.tracking.application.HabitRepository;
import habitquest.tracking.domain.Habit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Adapter
@Repository
public class InMemoryHabitRepository implements HabitRepository {

  private final Map<String, Habit> store = new HashMap<>();

  @Override
  public Habit save(Habit habit) {
    store.put(habit.getId(), habit);
    return habit;
  }

  @Override
  public Optional<Habit> findById(String id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public void deleteById(String id) {
    if (!store.containsKey(id)) {
      throw new HabitNotFoundException(id);
    }
    store.remove(id);
  }
}
