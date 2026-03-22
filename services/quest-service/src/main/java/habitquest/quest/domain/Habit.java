package habitquest.quest.domain;

import common.ddd.Aggregate;
import common.ddd.Id;
import habitquest.quest.domain.reminder.Recurrence;
import java.time.LocalDate;
import java.util.List;

public class Habit implements Aggregate<Id<Habit>> {
  private final Id<Habit> id;
  private List<Tag> tags;
  private String title;
  private String description;
  private Recurrence recurrence;
  private LocalDate lastAttendedDate;

  public Habit(Id<Habit> id) {
    this.id = id;
  }

  public void attendHabit(LocalDate date) {
    this.lastAttendedDate = date;
  }

  public LocalDate nextRecurrence() {
    return this.recurrence.nextAfter(this.lastAttendedDate);
  }

  public LocalDate nextRecurrence(LocalDate date) {
    return this.recurrence.nextAfter(date);
  }

  public List<Tag> getTags() {
    return tags;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public Id<Habit> getId() {
    return this.id;
  }
}
