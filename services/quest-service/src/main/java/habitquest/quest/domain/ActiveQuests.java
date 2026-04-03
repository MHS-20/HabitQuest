package habitquest.quest.domain;

import common.ddd.Aggregate;
import common.ddd.Id;
import habitquest.quest.domain.reminder.DailyRecurrence;
import habitquest.quest.domain.reminder.MonthlyRecurrence;
import habitquest.quest.domain.reminder.Recurrence;
import habitquest.quest.domain.reminder.WeeklyRecurrence;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Tracks per-avatar progress for a quest template during a concrete active time window. */
public class ActiveQuests implements Aggregate<Id<ActiveQuests>> {

  public enum Status {
    IN_PROGRESS,
    COMPLETED,
    EXPIRED
  }

  private final Id<ActiveQuests> id;
  private final Id<Quest> questId;
  private final Id<Avatar> avatarId;
  private final LocalDate startedOn;
  private final LocalDate endsOn;
  private final Map<Id<Habit>, Integer> requiredOccurrences;
  private final Map<Id<Habit>, Integer> attendedOccurrences;

  private Status status;
  private LocalDate completedOn;

  public ActiveQuests(
      Id<ActiveQuests> id,
      Id<Quest> questId,
      Id<Avatar> avatarId,
      LocalDate startedOn,
      LocalDate endsOn,
      List<Habit> habits) {
    this.id = Objects.requireNonNull(id);
    this.questId = Objects.requireNonNull(questId);
    this.avatarId = Objects.requireNonNull(avatarId);
    this.startedOn = Objects.requireNonNull(startedOn);
    this.endsOn = Objects.requireNonNull(endsOn);
    Objects.requireNonNull(habits);

    if (endsOn.isBefore(startedOn)) {
      throw new IllegalArgumentException("endsOn cannot be before startedOn");
    }

    this.requiredOccurrences = new LinkedHashMap<>();
    this.attendedOccurrences = new HashMap<>();

    for (Habit habit : habits) {
      int required = computeRequiredOccurrences(habit, startedOn, endsOn);
      this.requiredOccurrences.put(habit.getId(), required);
      this.attendedOccurrences.put(habit.getId(), 0);
    }

    this.status = Status.IN_PROGRESS;
    if (areAllHabitsCompleted(this.requiredOccurrences, this.attendedOccurrences)) {
      this.status = Status.COMPLETED;
      this.completedOn = startedOn;
    }
  }

  public static ActiveQuests fromQuest(
      Id<ActiveQuests> id, Id<Avatar> avatarId, LocalDate startedOn, Quest quest) {
    Objects.requireNonNull(quest);
    Duration duration = Objects.requireNonNull(quest.getDuration(), "quest duration is required");
    long durationInDays = Math.max(1, duration.toDays());

    LocalDate endsOn = startedOn.plusDays(durationInDays - 1);
    return new ActiveQuests(id, quest.getId(), avatarId, startedOn, endsOn, quest.getHabits());
  }

  public boolean recordAttendance(Id<Habit> habitId, LocalDate attendedOn) {
    Objects.requireNonNull(habitId);
    Objects.requireNonNull(attendedOn);

    if (status != Status.IN_PROGRESS) {
      return false;
    }

    if (attendedOn.isBefore(startedOn) || attendedOn.isAfter(endsOn)) {
      return false;
    }

    Integer required = requiredOccurrences.get(habitId);
    if (required == null || required == 0) {
      return false;
    }

    int current = attendedOccurrences.getOrDefault(habitId, 0);
    if (current >= required) {
      return false;
    }

    attendedOccurrences.put(habitId, current + 1);
    if (allHabitsCompleted()) {
      status = Status.COMPLETED;
      completedOn = attendedOn;
    }
    return true;
  }

  public void refreshStatus(LocalDate today) {
    Objects.requireNonNull(today);
    if (status == Status.IN_PROGRESS && today.isAfter(endsOn)) {
      status = Status.EXPIRED;
    }
  }

  public int remainingOccurrences(Id<Habit> habitId) {
    int required = requiredOccurrences.getOrDefault(habitId, 0);
    int attended = attendedOccurrences.getOrDefault(habitId, 0);
    return Math.max(0, required - attended);
  }

  public boolean allHabitsCompleted() {
    return areAllHabitsCompleted(requiredOccurrences, attendedOccurrences);
  }

  private static boolean areAllHabitsCompleted(
      Map<Id<Habit>, Integer> requiredOccurrences, Map<Id<Habit>, Integer> attendedOccurrences) {
    if (requiredOccurrences.isEmpty()) {
      return false;
    }

    for (Map.Entry<Id<Habit>, Integer> required : requiredOccurrences.entrySet()) {
      int done = attendedOccurrences.getOrDefault(required.getKey(), 0);
      if (done <= 0) {
        return false;
      }
    }
    return true;
  }

  public Map<Id<Habit>, Integer> getRequiredOccurrences() {
    return Map.copyOf(requiredOccurrences);
  }

  public Map<Id<Habit>, Integer> getAttendedOccurrences() {
    return Map.copyOf(attendedOccurrences);
  }

  public Id<Quest> getQuestId() {
    return questId;
  }

  public Id<Avatar> getAvatarId() {
    return avatarId;
  }

  public LocalDate getStartedOn() {
    return startedOn;
  }

  public LocalDate getEndsOn() {
    return endsOn;
  }

  public Status getStatus() {
    return status;
  }

  public LocalDate getCompletedOn() {
    return completedOn;
  }

  @Override
  public Id<ActiveQuests> getId() {
    return id;
  }

  private static int computeRequiredOccurrences(
      Habit habit, LocalDate startedOn, LocalDate endsOn) {
    if (habit == null || habit.getRecurrence() == null) {
      return 0;
    }

    LocalDate occurrence = firstOccurrenceOnOrAfter(habit.getRecurrence(), startedOn);
    int count = 0;

    while (occurrence != null && !occurrence.isAfter(endsOn)) {
      count++;
      LocalDate next = habit.getRecurrence().nextAfter(occurrence);
      if (next == null || !next.isAfter(occurrence)) {
        break;
      }
      occurrence = next;
    }

    return count;
  }

  private static LocalDate firstOccurrenceOnOrAfter(Recurrence recurrence, LocalDate start) {
    return switch (recurrence) {
      case DailyRecurrence ignored -> start;
      case WeeklyRecurrence weekly -> start.with(TemporalAdjusters.nextOrSame(weekly.dayOfWeek()));
      case MonthlyRecurrence monthly -> {
        int day = monthly.dayOfMonth();
        LocalDate candidate = start.withDayOfMonth(Math.min(day, start.lengthOfMonth()));
        if (candidate.isBefore(start)) {
          LocalDate nextMonth = start.plusMonths(1);
          yield nextMonth.withDayOfMonth(Math.min(day, nextMonth.lengthOfMonth()));
        }
        yield candidate;
      }
      default -> start;
    };
  }
}
