package habitquest.tracking.application;

import common.hexagonal.Adapter;
import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.events.HabitAttended;
import habitquest.tracking.domain.events.HabitDeleted;
import habitquest.tracking.domain.events.HabitObserver;
import habitquest.tracking.domain.factory.HabitFactory;
import habitquest.tracking.domain.reminder.Recurrence;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Adapter
@Service
public class HabitServiceImpl implements HabitService {

  private final HabitRepository habitRepository;
  private final HabitFactory habitFactory;
  private final HabitObserver habitObserver;

  public HabitServiceImpl(
      HabitRepository habitRepository, HabitFactory habitFactory, HabitObserver habitObserver) {
    this.habitRepository = habitRepository;
    this.habitFactory = habitFactory;
    this.habitObserver = habitObserver;
  }

  @Override
  public Habit createHabit(Habit habit) {
    return habitRepository.save(habit);
  }

  @Override
  public Habit getHabitById(String habitId) throws HabitNotFoundException {
    return habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
  }

  @Override
  public void deleteHabitById(String habitId) throws HabitNotFoundException {
    habitRepository.deleteById(habitId);
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
  public LocalDate getLastAttendedDate(String habitId) throws HabitNotFoundException {
    return habitRepository
        .findById(habitId)
        .orElseThrow(() -> new HabitNotFoundException(habitId))
        .getLastAttendedDate();
  }

  @Override
  public Habit updateTitle(String habitId, String title) throws HabitNotFoundException {
    Habit habit =
        habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
    habit.setTitle(title);
    habitRepository.save(habit);
    return habit;
  }

  @Override
  public Habit updateDescription(String habitId, String description) throws HabitNotFoundException {
    Habit habit =
        habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
    habit.setDescription(description);
    habitRepository.save(habit);
    return habit;
  }

  @Override
  public Habit updateTags(String habitId, List<Tag> tags) throws HabitNotFoundException {
    Habit habit =
        habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
    habit.setTags(tags);
    habitRepository.save(habit);
    return habit;
  }

  @Override
  public Habit updateRecurrence(String habitId, Recurrence recurrence)
      throws HabitNotFoundException {
    Habit habit =
        habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
    habit.setRecurrence(recurrence);
    habitRepository.save(habit);
    return habit;
  }

  @Override
  public Habit attendHabit(String habitId, LocalDate date) throws HabitNotFoundException {
    Habit habit =
        habitRepository.findById(habitId).orElseThrow(() -> new HabitNotFoundException(habitId));
    habit.attendHabit(date);
    habitRepository.save(habit);
    habitObserver.notifyHabitEvent(new HabitAttended(habit));
    return habit;
  }
}
