package habitquest.tracking.application.service;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.tracking.application.exceptions.HabitNotFoundException;
import habitquest.tracking.application.port.in.HabitCommandService;
import habitquest.tracking.application.port.out.*;
import habitquest.tracking.domain.*;
import habitquest.tracking.domain.events.*;
import habitquest.tracking.domain.factory.HabitFactory;
import habitquest.tracking.domain.reminder.Recurrence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Adapter
@Service
public class HabitCommandServiceImpl implements HabitCommandService {

  private static final int ATTEND_HABIT_XP_REWARD = 10;
  private static final int NOT_ATTENDED_DAMAGE = 10;

  private final HabitRepository habitRepository;
  private final HabitHistoryRepository historyRepository;
  private final HabitFactory habitFactory;
  private final HabitObserver habitObserver;
  private final AvatarClientPort avatarClient;
  private final QuestClientPort questClient;

  public HabitCommandServiceImpl(
      HabitRepository habitRepository,
      HabitHistoryRepository historyRepository,
      HabitFactory habitFactory,
      HabitObserver habitObserver,
      AvatarClientPort avatarClient,
      QuestClientPort questClient) {
    this.habitRepository = habitRepository;
    this.historyRepository = historyRepository;
    this.habitFactory = habitFactory;
    this.habitObserver = habitObserver;
    this.avatarClient = avatarClient;
    this.questClient = questClient;
  }

  private Habit findOrThrow(Id<Habit> habitId) throws HabitNotFoundException {
    return habitRepository
        .findById(habitId)
        .orElseThrow(() -> new HabitNotFoundException(habitId.value()));
  }

  @Override
  public Habit createHabit(
      Id<Avatar> avatarId,
      String title,
      String description,
      Recurrence recurrence,
      String associatedQuestId,
      String sourceHabitId) {
    Habit habit =
        habitFactory.createHabit(
            avatarId, title, description, recurrence, associatedQuestId, sourceHabitId);
    Habit saved = habitRepository.save(habit);
    appendHistory(
        new HabitCreated(saved, saved.getAvatarId()),
        "recurrence=" + recurrence.getClass().getSimpleName());
    return saved;
  }

  @Override
  public Habit updateTitle(Id<Habit> habitId, String title) throws HabitNotFoundException {
    Habit habit = findOrThrow(habitId);
    habit.setTitle(title);
    habitRepository.save(habit);
    appendHistory(new HabitUpdated(habit, habit.getAvatarId()), "title=" + title);
    return habit;
  }

  @Override
  public Habit updateDescription(Id<Habit> habitId, String description)
      throws HabitNotFoundException {
    Habit habit = findOrThrow(habitId);
    habit.setDescription(description);
    habitRepository.save(habit);
    appendHistory(new HabitUpdated(habit, habit.getAvatarId()), "description updated");
    return habit;
  }

  @Override
  public Habit updateTags(Id<Habit> habitId, List<Tag> tags) throws HabitNotFoundException {
    Habit habit = findOrThrow(habitId);
    habit.setTags(tags);
    habitRepository.save(habit);
    appendHistory(
        new HabitUpdated(habit, habit.getAvatarId()), "tags updated count=" + tags.size());
    return habit;
  }

  @Override
  public Habit updateRecurrence(Id<Habit> habitId, Recurrence recurrence)
      throws HabitNotFoundException {
    Habit habit = findOrThrow(habitId);
    habit.setRecurrence(recurrence);
    habitRepository.save(habit);
    appendHistory(
        new HabitUpdated(habit, habit.getAvatarId()),
        "recurrence=" + recurrence.getClass().getSimpleName());
    return habit;
  }

  @Override
  public Habit attendHabit(Id<Habit> habitId, LocalDateTime date) throws HabitNotFoundException {
    Habit habit = findOrThrow(habitId);
    habit.attendHabit(date);
    habitRepository.save(habit);
    avatarClient.grantExperience(habit.getAvatarId(), ATTEND_HABIT_XP_REWARD);
    habit
        .getAssociatedQuestId()
        .ifPresent(
            questId -> {
              Id<Habit> questHabitId =
                  habit.getSourceHabitId().map(Id<Habit>::new).orElse(habit.getId());
              questClient.recordHabitAttendance(
                  questId, habit.getAvatarId(), questHabitId, date.toLocalDate());
            });
    HabitAttended event = new HabitAttended(habit, habit.getAvatarId());
    appendHistory(event, "attendedAt=" + date);
    habitObserver.notifyHabitEvent(event);
    return habit;
  }

  @Override
  public void deleteHabitById(Id<Habit> habitId) throws HabitNotFoundException {
    Habit habit = findOrThrow(habitId);
    habitRepository.deleteById(habitId);
    HabitDeleted event = new HabitDeleted(habitId, habit.getAvatarId());
    appendHistory(event, "habit deleted");
    habitObserver.notifyHabitEvent(event);
  }

  @Override
  public void detectOverdueHabits() {
    LocalDateTime now = LocalDateTime.now();
    for (Habit habit : habitRepository.findAll()) {
      LocalDateTime lastAttendedDate = habit.getLastAttendedDate();
      if (lastAttendedDate == null) {
        HabitNotAttended event = new HabitNotAttended(habit, habit.getAvatarId());
        boolean appended = appendNotAttendedHistoryIfNew(habit, "never-attended", event);
        if (appended) {
          avatarClient.applyDamage(habit.getAvatarId(), NOT_ATTENDED_DAMAGE);
        }
        habitObserver.notifyHabitEvent(event);
        continue;
      }
      LocalDateTime nextExpected = habit.nextRecurrence();
      if (nextExpected.isBefore(now)) {
        HabitNotAttended event = new HabitNotAttended(habit, habit.getAvatarId());
        boolean appended =
            appendNotAttendedHistoryIfNew(habit, "expectedAt=" + nextExpected, event);
        if (appended) {
          avatarClient.applyDamage(habit.getAvatarId(), NOT_ATTENDED_DAMAGE);
        }
        habitObserver.notifyHabitEvent(event);
      }
    }
  }

  private void appendHistory(HabitEvent event, String details) {
    historyRepository.append(new HabitHistoryEvent(event, LocalDateTime.now(), details));
  }

  private boolean appendNotAttendedHistoryIfNew(
      Habit habit, String marker, HabitNotAttended notAttendedEvent) {
    List<HabitHistoryEvent> history = historyRepository.findByHabitId(habit.getId());
    HabitHistoryEvent last = history.isEmpty() ? null : history.getLast();
    if (last == null
        || !(last.event() instanceof HabitNotAttended)
        || !Objects.equals(last.details(), marker)) {
      appendHistory(notAttendedEvent, marker);
      return true;
    }
    return false;
  }
}
