package habitquest.tracking.domain.factory;

import common.ddd.Factory;
import common.ddd.Id;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.reminder.Recurrence;
import java.util.Optional;

public class HabitFactory implements Factory {

  private final IdGenerator idGenerator;

  public HabitFactory(IdGenerator idGenerator) {
    this.idGenerator = idGenerator;
  }

  public Habit createHabit(
      Id<Avatar> avatarId,
      String title,
      String description,
      Recurrence recurrence,
      String associatedQuestId,
      String sourceHabitId) {
    return new Habit(
        new Id<>(this.idGenerator.nextId()),
        avatarId,
        title,
        description,
        recurrence,
        Optional.ofNullable(associatedQuestId),
        Optional.ofNullable(sourceHabitId));
  }
}
