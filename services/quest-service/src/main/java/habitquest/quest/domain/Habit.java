package habitquest.quest.domain;

import common.ddd.Aggregate;
import habitquest.quest.domain.reminder.Recurrence;
import java.time.LocalDate;
import java.util.List;

public class Habit implements Aggregate<String> {
  private final String id;
  private List<Tag> tags;
  private String title;
  private String description;
  private Recurrence recurrence;
  private LocalDate lastAttendedDate;

  public Habit(String id) {
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
  public String getId() {
    return this.id;
  }
}
