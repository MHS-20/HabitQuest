package habitquest.tracking.application;

import common.hexagonal.Adapter;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.events.HabitAttended;
import habitquest.tracking.domain.events.HabitCreated;
import habitquest.tracking.domain.events.HabitDeleted;
import habitquest.tracking.domain.events.HabitEvent;
import habitquest.tracking.domain.events.HabitHistoryEvent;
import habitquest.tracking.domain.events.HabitNotAttended;
import habitquest.tracking.domain.events.HabitObserver;
import habitquest.tracking.domain.events.HabitUpdated;
import habitquest.tracking.domain.factory.HabitFactory;
import habitquest.tracking.domain.reminder.Recurrence;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Adapter
@Service
public class HabitServiceImpl implements HabitService {

  private final HabitRepository habitRepository;
  private final HabitHistoryRepository historyRepository;
  private final HabitFactory habitFactory;
  private final HabitObserver habitObserver;

  public HabitServiceImpl(
      HabitRepository habitRepository,
      HabitHistoryRepository historyRepository,
      HabitFactory habitFactory,
      HabitObserver habitObserver) {
    this.habitRepository = habitRepository;
    this.historyRepository = historyRepository;
    this.habitFactory = habitFactory;
    this.habitObserver = habitObserver;
  }

  @Override
  public Habit createDailyHabit(String avatarId, String name, String description) {
    Habit habit = habitFactory.createDailyHabit(avatarId, name, description);
    Habit saved = habitRepository.save(habit);
    appendHistory(new HabitCreated(saved), "daily recurrence");
    return saved;
  }

  @Override
  public Habit createWeeklyHabit(
      String avatarId, String title, String description, DayOfWeek dayOfWeek) {
    Habit habit = habitFactory.createWeeklyHabit(avatarId, title, description, dayOfWeek);
    Habit saved = habitRepository.save(habit);
    appendHistory(new HabitCreated(saved), "weekly recurrence day=" + dayOfWeek);
    return saved;
  }

  @Override
  public Habit createMonthlyHabit(
      String avatarId, String title, String description, Integer dayOfMonth) {
    Habit habit = habitFactory.createMonthlyHabit(avatarId, title, description, dayOfMonth);
    Habit saved = habitRepository.save(habit);
    appendHistory(new HabitCreated(saved), "monthly recurrence day=" + dayOfMonth);
    return saved;
  }

  @Override
  public Habit getHabitById(String habitId) throws HabitNotFoundException {
    return habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
  }

  @Override
  public void deleteHabitById(String habitId) throws HabitNotFoundException {
    getHabitById(habitId);
    habitRepository.deleteById(habitId);
    appendHistory(new HabitDeleted(habitId), "habit deleted");
    habitObserver.notifyHabitEvent(new HabitDeleted(habitId));
  }

  @Override
  public String getTitle(String habitId) throws HabitNotFoundException {
    return habitRepository
        .findById(habitId)
        .orElseThrow(() -> new HabitNotFoundException(habitId))
        .getTitle();
  }

  @Override
  public String getDescription(String habitId) throws HabitNotFoundException {
    return habitRepository
        .findById(habitId)
        .orElseThrow(() -> new HabitNotFoundException(habitId))
        .getDescription();
  }

  @Override
  public List<Tag> getTags(String habitId) throws HabitNotFoundException {
    return habitRepository
        .findById(habitId)
        .orElseThrow(() -> new HabitNotFoundException(habitId))
        .getTags();
  }

  @Override
  public Recurrence getRecurrence(String habitId) throws HabitNotFoundException {
    return habitRepository
        .findById(habitId)
        .orElseThrow(() -> new HabitNotFoundException(habitId))
        .getRecurrence();
  }

  @Override
  public LocalDateTime getLastAttendedDate(String habitId) throws HabitNotFoundException {
    return habitRepository
        .findById(habitId)
        .orElseThrow(() -> new HabitNotFoundException(habitId))
        .getLastAttendedDate();
  }

  @Override
  public List<HabitHistoryEvent> getHistory(String habitId) {
    getHabitById(habitId);
    return historyRepository.findByHabitId(habitId);
  }

  @Override
  public Habit updateTitle(String habitId, String title) throws HabitNotFoundException {
    Habit habit =
        habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
    habit.setTitle(title);
    habitRepository.save(habit);
    appendHistory(new HabitUpdated(habit), "title=" + title);
    return habit;
  }

  @Override
  public Habit updateDescription(String habitId, String description) throws HabitNotFoundException {
    Habit habit =
        habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
    habit.setDescription(description);
    habitRepository.save(habit);
    appendHistory(new HabitUpdated(habit), "description updated");
    return habit;
  }

  @Override
  public Habit updateTags(String habitId, List<Tag> tags) throws HabitNotFoundException {
    Habit habit =
        habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
    habit.setTags(tags);
    habitRepository.save(habit);
    appendHistory(new HabitUpdated(habit), "tags updated count=" + tags.size());
    return habit;
  }

  @Override
  public Habit updateRecurrence(String habitId, Recurrence recurrence)
      throws HabitNotFoundException {
    Habit habit =
        habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
    habit.setRecurrence(recurrence);
    habitRepository.save(habit);
    appendHistory(new HabitUpdated(habit), "recurrence=" + recurrence.getClass().getSimpleName());
    return habit;
  }

  @Override
  public Habit attendHabit(String habitId, LocalDateTime date) throws HabitNotFoundException {
    Habit habit =
        habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
    habit.attendHabit(date);
    habitRepository.save(habit);
    appendHistory(new HabitAttended(habit), "attendedAt=" + date);
    habitObserver.notifyHabitEvent(new HabitAttended(habit));
    return habit;
  }

  @Override
  public void detectOverdueHabits() {
    LocalDateTime now = LocalDateTime.now();
    List<Habit> habits = habitRepository.findAll();
    for (Habit habit : habits) {
      LocalDateTime lastAttendedDate = habit.getLastAttendedDate();
      if (lastAttendedDate == null) {
        appendNotAttendedHistoryIfNew(habit, "never-attended", new HabitNotAttended(habit));
        habitObserver.notifyHabitEvent(new HabitNotAttended(habit));
        continue;
      }
      LocalDateTime nextExpected = habit.getRecurrence().nextAfter(lastAttendedDate);
      if (nextExpected.isBefore(now)) {
        appendNotAttendedHistoryIfNew(
            habit, "expectedAt=" + nextExpected, new HabitNotAttended(habit));
        habitObserver.notifyHabitEvent(new HabitNotAttended(habit));
      }
    }
  }

  private void appendHistory(HabitEvent event, String details) {
    historyRepository.append(new HabitHistoryEvent(event, LocalDateTime.now(), details));
  }

  private void appendNotAttendedHistoryIfNew(
      Habit habit, String marker, HabitNotAttended notAttendedEvent) {
    List<HabitHistoryEvent> history = historyRepository.findByHabitId(habit.getId());
    HabitHistoryEvent last = history.isEmpty() ? null : history.getLast();
    if (last == null
        || !(last.event() instanceof HabitNotAttended)
        || !Objects.equals(last.details(), marker)) {
      appendHistory(notAttendedEvent, marker);
    }
  }
}
