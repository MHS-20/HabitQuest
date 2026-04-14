package habitquest.tracking.application.port.out;

import common.ddd.Id;
import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;
import java.util.List;
import java.util.Optional;

@OutBoundPort
public interface HabitRepository extends Repository {
  Habit save(Habit habit);

  Optional<Habit> findById(Id<Habit> id);

  void deleteById(Id<Habit> id);

  List<Habit> findAll();

  List<Habit> findByAvatarId(Id<Avatar> avatarId);
}
