package habitquest.tracking.application;

import habitquest.tracking.domain.Habit;
import habitquest.tracking.domain.Tag;
import habitquest.tracking.domain.events.HabitObserver;
import habitquest.tracking.domain.factory.HabitFactory;
import habitquest.tracking.domain.reminder.Recurrence;
import java.time.LocalDate;
import java.util.List;

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
    return null;
  }

  @Override
  public Habit getHabitById(String id) throws HabitNotFoundException {
    return null;
  }

  @Override
  public void deleteHabitById(String id) throws HabitNotFoundException {}

  @Override
  public String getTitle(String habitId) throws HabitNotFoundException {
    return habitRepository.findById(habitId).getTitle();
  }

  @Override
  public String getDescription(String habitId) throws HabitNotFoundException {
    return habitRepository.findById(habitId).getDescription();
  }

  @Override
  public List<Tag> getTags(String habitId) throws HabitNotFoundException {
    return habitRepository.findById(habitId).getTags();
  }

  @Override
  public Recurrence getRecurrence(String habitId) throws HabitNotFoundException {
    return habitRepository.findById(habitId).getRecurrence();
  }

  @Override
  public LocalDate getLastAttendedDate(String habitId) throws HabitNotFoundException {
    return habitRepository.findById(habitId).getLastAttendedDate();
  }

  @Override
  public Habit updateTitle(String habitId, String title) throws HabitNotFoundException {
    Habit habit = habitRepository.findById(habitId);
    habit.setTitle(title);
    habitRepository.save(habit);
    return habit;
  }

  @Override
  public Habit updateDescription(String habitId, String description) throws HabitNotFoundException {
    Habit habit = habitRepository.findById(habitId);
    habit.setDescription(description);
    habitRepository.save(habit);
    return habit;
  }

  @Override
  public Habit updateTags(String habitId, List<Tag> tags) throws HabitNotFoundException {
    Habit habit = habitRepository.findById(habitId);
    habit.setTags(tags);
    habitRepository.save(habit);
    return habit;
  }

  @Override
  public Habit updateRecurrence(String habitId, Recurrence recurrence)
      throws HabitNotFoundException {
    Habit habit = habitRepository.findById(habitId);
    habit.setRecurrence(recurrence);
    habitRepository.save(habit);
    return habit;
  }

  @Override
  public Habit attendHabit(String habitId, LocalDate date) throws HabitNotFoundException {
    Habit habit = habitRepository.findById(habitId);
    habit.attendHabit(date);
    habitRepository.save(habit);
    return habit;
  }
}
