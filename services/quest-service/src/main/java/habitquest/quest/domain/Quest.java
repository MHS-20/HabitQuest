package habitquest.quest.domain;

import common.ddd.Aggregate;
import common.ddd.Id;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Quest implements Aggregate<Id<Quest>> {

  private final Id<Quest> id;
  private String name;
  private final List<Habit> habits;
  private Duration duration;
  private Reward reward;

  public Quest(Id<Quest> id) {
    this.id = id;
    this.habits = new ArrayList<>();
  }

  public Quest(Id<Quest> id, String name) {
    this.id = id;
    this.name = name;
    this.habits = new ArrayList<>();
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

  public Reward getReward() {
    return reward;
  }

  public void setReward(Reward reward) {
    this.reward = reward;
  }

  public List<Habit> getHabits() {
    return habits;
  }

  public void addHabit(Habit habit) {
    this.habits.add(habit);
  }

  public void removeHabit(Habit habit) {
    this.habits.remove(habit);
  }

  @Override
  public Id<Quest> getId() {
    return this.id;
  }
}
