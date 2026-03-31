package habitquest.tracking.application;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.tracking.domain.Avatar;
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
  public Habit createDailyHabit(Id<Avatar> avatarId, String name, String description) {
    Habit habit = habitFactory.createDailyHabit(avatarId, name, description);
    Habit saved = habitRepository.save(habit);
    appendHistory(new HabitCreated(saved, saved.getAvatarId()), "daily recurrence");
    return saved;
  }

  @Override
  public Habit createWeeklyHabit(
      Id<Avatar> avatarId, String title, String description, DayOfWeek dayOfWeek) {
    Habit habit = habitFactory.createWeeklyHabit(avatarId, title, description, dayOfWeek);
    Habit saved = habitRepository.save(habit);
    appendHistory(
        new HabitCreated(saved, saved.getAvatarId()), "weekly recurrence day=" + dayOfWeek);
    return saved;
  }

  @Override
  public Habit createMonthlyHabit(
      Id<Avatar> avatarId, String title, String description, Integer dayOfMonth) {
    Habit habit = habitFactory.createMonthlyHabit(avatarId, title, description, dayOfMonth);
    Habit saved = habitRepository.save(habit);
    appendHistory(
        new HabitCreated(saved, saved.getAvatarId()), "monthly recurrence day=" + dayOfMonth);
    return saved;
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
  public void deleteHabitById(Id<Habit> habitId) throws HabitNotFoundException {
    Habit habit = getHabitById(habitId);
    habitRepository.deleteById(habitId);
    HabitDeleted event = new HabitDeleted(habitId, habit.getAvatarId());
    appendHistory(event, "habit deleted");
    habitObserver.notifyHabitEvent(event);
  }

  @Override
  public String getTitle(Id<Habit> habitId) throws HabitNotFoundException {
    return habitRepository
        .findById(habitId)
        .orElseThrow(() -> new HabitNotFoundException(habitId.value()))
        .getTitle();
  }

  @Override
  public String getDescription(Id<Habit> habitId) throws HabitNotFoundException {
    return habitRepository
        .findById(habitId)
        .orElseThrow(() -> new HabitNotFoundException(habitId.value()))
        .getDescription();
  }

  @Override
  public List<Tag> getTags(Id<Habit> habitId) throws HabitNotFoundException {
    return habitRepository
        .findById(habitId)
        .orElseThrow(() -> new HabitNotFoundException(habitId.value()))
        .getTags();
  }

  @Override
  public Recurrence getRecurrence(Id<Habit> habitId) throws HabitNotFoundException {
    return habitRepository
        .findById(habitId)
        .orElseThrow(() -> new HabitNotFoundException(habitId.value()))
        .getRecurrence();
  }

  @Override
  public LocalDateTime getLastAttendedDate(Id<Habit> habitId) throws HabitNotFoundException {
    return habitRepository
        .findById(habitId)
        .orElseThrow(() -> new HabitNotFoundException(habitId.value()))
        .getLastAttendedDate();
  }

  @Override
  public List<HabitHistoryEvent> getHistory(Id<Habit> habitId) {
    getHabitById(habitId);
    return historyRepository.findByHabitId(habitId);
  }

  @Override
  public Habit updateTitle(Id<Habit> habitId, String title) throws HabitNotFoundException {
    Habit habit =
        habitRepository
            .findById(habitId)
            .orElseThrow(() -> new HabitNotFoundException(habitId.value()));
    habit.setTitle(title);
    habitRepository.save(habit);
    appendHistory(new HabitUpdated(habit, habit.getAvatarId()), "title=" + title);
    return habit;
  }

  @Override
  public Habit updateDescription(Id<Habit> habitId, String description)
      throws HabitNotFoundException {
    Habit habit =
        habitRepository
            .findById(habitId)
            .orElseThrow(() -> new HabitNotFoundException(habitId.value()));
    habit.setDescription(description);
    habitRepository.save(habit);
    appendHistory(new HabitUpdated(habit, habit.getAvatarId()), "description updated");
    return habit;
  }

  @Override
  public Habit updateTags(Id<Habit> habitId, List<Tag> tags) throws HabitNotFoundException {
    Habit habit =
        habitRepository
            .findById(habitId)
            .orElseThrow(() -> new HabitNotFoundException(habitId.value()));
    habit.setTags(tags);
    habitRepository.save(habit);
    appendHistory(
        new HabitUpdated(habit, habit.getAvatarId()), "tags updated count=" + tags.size());
    return habit;
  }

  @Override
  public Habit updateRecurrence(Id<Habit> habitId, Recurrence recurrence)
      throws HabitNotFoundException {
    Habit habit =
        habitRepository
            .findById(habitId)
            .orElseThrow(() -> new HabitNotFoundException(habitId.value()));
    habit.setRecurrence(recurrence);
    habitRepository.save(habit);
    appendHistory(
        new HabitUpdated(habit, habit.getAvatarId()),
        "recurrence=" + recurrence.getClass().getSimpleName());
    return habit;
  }

  @Override
  public Habit attendHabit(Id<Habit> habitId, LocalDateTime date) throws HabitNotFoundException {
    Habit habit =
        habitRepository
            .findById(habitId)
            .orElseThrow(() -> new HabitNotFoundException(habitId.value()));
    habit.attendHabit(date);
    habitRepository.save(habit);
    HabitAttended event = new HabitAttended(habit, habit.getAvatarId());
    appendHistory(event, "attendedAt=" + date);
    habitObserver.notifyHabitEvent(event);
    return habit;
  }

  @Override
  public void detectOverdueHabits() {
    LocalDateTime now = LocalDateTime.now();
    List<Habit> habits = habitRepository.findAll();
    for (Habit habit : habits) {
      LocalDateTime lastAttendedDate = habit.getLastAttendedDate();
      if (lastAttendedDate == null) {
        HabitNotAttended event = new HabitNotAttended(habit, habit.getAvatarId());
        appendNotAttendedHistoryIfNew(habit, "never-attended", event);
        habitObserver.notifyHabitEvent(event);
        continue;
      }
      LocalDateTime nextExpected = habit.nextRecurrence();
      if (nextExpected.isBefore(now)) {
        HabitNotAttended event = new HabitNotAttended(habit, habit.getAvatarId());
        appendNotAttendedHistoryIfNew(habit, "expectedAt=" + nextExpected, event);
        habitObserver.notifyHabitEvent(event);
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
