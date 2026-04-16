package habitquest.tracking.application.service;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.tracking.application.exceptions.HabitNotFoundException;
import habitquest.tracking.application.port.in.HabitQueryService;
import habitquest.tracking.application.port.out.HabitHistoryRepository;
import habitquest.tracking.application.port.out.HabitRepository;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.events.HabitHistoryEvent;
import habitquest.tracking.domain.reminder.Recurrence;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Adapter
@Service
public class HabitQueryServiceImpl implements HabitQueryService {

  private final HabitRepository habitRepository;
  private final HabitHistoryRepository historyRepository;

  public HabitQueryServiceImpl(
      HabitRepository habitRepository, HabitHistoryRepository historyRepository) {
    this.habitRepository = habitRepository;
    this.historyRepository = historyRepository;
  }

  @Override
  public Habit getHabitById(Id<Habit> habitId) throws HabitNotFoundException {
    return habitRepository
        .findById(habitId)
        .orElseThrow(() -> new HabitNotFoundException(habitId.value()));
  }

  @Override
  public List<Habit> getHabitsByAvatarId(Id<Avatar> avatarId) {
    return habitRepository.findByAvatarId(avatarId);
  }

  @Override
  public String getTitle(Id<Habit> habitId) throws HabitNotFoundException {
    return getHabitById(habitId).getTitle();
  }

  @Override
  public String getDescription(Id<Habit> habitId) throws HabitNotFoundException {
    return getHabitById(habitId).getDescription();
  }

  @Override
  public List<Tag> getTags(Id<Habit> habitId) throws HabitNotFoundException {
    return getHabitById(habitId).getTags();
  }

  @Override
  public Recurrence getRecurrence(Id<Habit> habitId) throws HabitNotFoundException {
    return getHabitById(habitId).getRecurrence();
  }

  @Override
  public LocalDateTime getLastAttendedDate(Id<Habit> habitId) throws HabitNotFoundException {
    return getHabitById(habitId).getLastAttendedDate();
  }

  @Override
  public List<HabitHistoryEvent> getHistory(Id<Habit> habitId) {
    getHabitById(habitId);
    return historyRepository.findByHabitId(habitId);
  }

  @Override
  public List<HabitHistoryEvent> getHistoryByAvatarId(Id<Avatar> avatarId) {
    return habitRepository.findByAvatarId(avatarId).stream()
        .flatMap(habit -> historyRepository.findByHabitId(habit.getId()).stream())
        .sorted(Comparator.comparing(HabitHistoryEvent::occurredAt).reversed())
        .toList();
  }
}
