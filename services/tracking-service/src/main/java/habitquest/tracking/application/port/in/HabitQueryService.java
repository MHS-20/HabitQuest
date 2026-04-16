package habitquest.tracking.application.port.in;

import common.ddd.Id;
import habitquest.tracking.application.exceptions.HabitNotFoundException;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.events.HabitHistoryEvent;
import habitquest.tracking.domain.reminder.Recurrence;
import java.time.LocalDateTime;
import java.util.List;

public interface HabitQueryService {

  Habit getHabitById(Id<Habit> habitId) throws HabitNotFoundException;

  List<Habit> getHabitsByAvatarId(Id<Avatar> avatarId);

  String getTitle(Id<Habit> habitId) throws HabitNotFoundException;

  String getDescription(Id<Habit> habitId) throws HabitNotFoundException;

  List<Tag> getTags(Id<Habit> habitId) throws HabitNotFoundException;

  Recurrence getRecurrence(Id<Habit> habitId) throws HabitNotFoundException;

  LocalDateTime getLastAttendedDate(Id<Habit> habitId) throws HabitNotFoundException;

  List<HabitHistoryEvent> getHistory(Id<Habit> habitId);

  List<HabitHistoryEvent> getHistoryByAvatarId(Id<Avatar> avatarId);
}
