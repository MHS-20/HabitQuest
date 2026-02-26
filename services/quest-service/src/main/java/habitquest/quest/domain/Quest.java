package habitquest.quest.domain;

import common.ddd.Aggregate;
import java.time.Duration;
import java.util.List;

public class Quest implements Aggregate<String> {

  private String id;
  private List<Habit> habits;
  private Duration duration;
  private Reward reward;

  public Quest(String id) {
    this.id = id;
  }

  public void addHabit(Habit habit) {
    this.habits.add(habit);
  }

  public void removeHabit(Habit habit) {
    this.habits.remove(habit);
  }

  @Override
  public String getId() {
    return this.id;
  }
}
