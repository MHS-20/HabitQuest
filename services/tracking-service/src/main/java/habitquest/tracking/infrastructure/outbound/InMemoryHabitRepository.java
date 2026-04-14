package habitquest.tracking.infrastructure.outbound;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.tracking.application.exceptions.HabitNotFoundException;
import habitquest.tracking.application.port.out.HabitRepository;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Adapter
@Repository
public class InMemoryHabitRepository implements HabitRepository {

  private final Map<Id<Habit>, Habit> store = new HashMap<>();

  @Override
  public Habit save(Habit habit) {
    store.put(habit.getId(), habit);
    return habit;
  }

  @Override
  public Optional<Habit> findById(Id<Habit> id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public void deleteById(Id<Habit> id) {
    if (!store.containsKey(id)) {
      throw new HabitNotFoundException(id.value());
    }
    store.remove(id);
  }

  @Override
  public List<Habit> findAll() {
    return List.copyOf(store.values());
  }

  @Override
  public List<Habit> findByAvatarId(Id<Avatar> avatarId) {
    return store.values().stream().filter(habit -> habit.getAvatarId().equals(avatarId)).toList();
  }
}
