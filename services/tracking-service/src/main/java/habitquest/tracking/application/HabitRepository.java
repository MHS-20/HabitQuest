package habitquest.tracking.application;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.tracking.domain.Habit;
import java.util.Optional;

@OutBoundPort
public interface HabitRepository extends Repository {
  Habit save(Habit habit);

  Optional<Habit> findById(String id);

  void deleteById(String id);
}
