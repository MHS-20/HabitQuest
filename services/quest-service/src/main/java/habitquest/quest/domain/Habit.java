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

  public Habit(
      Id<Habit> id, String title, String description, List<Tag> tags, Recurrence recurrence) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.tags = tags;
    this.recurrence = recurrence;
  }

  public void attendHabit(LocalDate date) {
    this.lastAttendedDate = date;
  }

  public LocalDate getLastAttendedDate() {
    return lastAttendedDate;
  }

  public Recurrence getRecurrence() {
    return recurrence;
  }

  public LocalDate nextRecurrence() {
    if (this.lastAttendedDate == null) {
      return null;
    } else {
      return this.recurrence.nextAfter(this.lastAttendedDate);
    }
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

  public void setTags(List<Tag> tags) {
    this.tags = tags;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setRecurrence(Recurrence recurrence) {
    this.recurrence = recurrence;
  }

  @Override
  public Id<Habit> getId() {
    return this.id;
  }
}
