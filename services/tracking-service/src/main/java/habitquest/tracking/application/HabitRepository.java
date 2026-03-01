package habitquest.tracking.application;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.tracking.domain.Habit;

@OutBoundPort
public interface HabitRepository extends Repository {
  Habit save(Habit habit);

  Habit findById(String id) throws HabitNotFoundException;

  void deleteById(String id) throws HabitNotFoundException;
}
