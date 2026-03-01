package habitquest.tracking.domain;

import common.ddd.Aggregate;
import habitquest.tracking.domain.reminder.Recurrence;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class Habit implements Aggregate<String> {
  private final String id;
  private List<Tag> tags;
  private String title;
  private String description;
  private Recurrence recurrence;
  private LocalDate lastAttendedDate;
  private final String avatarId;
  private final Optional<String> associatedQuestId;

  public Habit(
      String id,
      String avatarId,
      String title,
      String description,
      Recurrence recurrence,
      Optional<String> associatedQuestId) {
    this.id = id;
    this.avatarId = avatarId;
    this.associatedQuestId = associatedQuestId;
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

  public String getAvatarId() {
    return avatarId;
  }

  public Optional<String> getAssociatedQuestId() {
    return associatedQuestId;
  }

  @Override
  public String getId() {
    return this.id;
  }
}
