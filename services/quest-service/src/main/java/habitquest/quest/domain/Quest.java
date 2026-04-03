package habitquest.quest.domain;

import common.ddd.Aggregate;
import common.ddd.Id;
import habitquest.quest.domain.reminder.DailyRecurrence;
import habitquest.quest.domain.reminder.MonthlyRecurrence;
import habitquest.quest.domain.reminder.WeeklyRecurrence;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Quest implements Aggregate<Id<Quest>> {

  private final Id<Quest> id;
  private String name;
  private final List<Habit> habits;
  private Duration duration;
  private MoneyReward reward;

  public Quest(Id<Quest> id) {
    this.id = id;
    this.habits = new ArrayList<>();
  }

  public Quest(Id<Quest> id, String name) {
    this.id = id;
    this.name = name;
    this.habits = new ArrayList<>();
  }

  public Quest(Id<Quest> id, String name, Duration duration) {
    this(id, name);
    this.duration = duration;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Duration getDuration() {
    return duration;
  }

  public void setDuration(Duration duration) {
    this.duration = duration;
  }

  public MoneyReward getReward() {
    return reward;
  }

  public void setReward(MoneyReward reward) {
    this.reward = reward;
  }

  public List<Habit> getHabits() {
    return habits;
  }

  public void addHabit(Habit habit) {
    this.habits.add(habit);
  }

  public Integer getTotalNumberOfHabits() {
    long durationInDays = this.getDuration().toDays();
    int result = 0;
    for (Habit h : this.getHabits()) {
      switch (h.getRecurrence()) {
        case DailyRecurrence d:
          result += (int) durationInDays;
          break;
        case WeeklyRecurrence w:
          result += (int) durationInDays / 7;
          break;
        case MonthlyRecurrence m:
          result += (int) durationInDays / 30;
          break;
        default:
          result += 0;
      }
    }
    return result;
  }

  public void removeHabit(Id<Habit> habitId) {
    this.habits.removeIf(h -> h.getId().equals(habitId));
  }

  @Override
  public Id<Quest> getId() {
    return this.id;
  }
}
