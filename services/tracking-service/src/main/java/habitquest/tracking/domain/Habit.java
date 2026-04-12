package habitquest.tracking.domain;

import common.ddd.Aggregate;
import common.ddd.Id;
import habitquest.tracking.domain.reminder.Recurrence;
import java.time.LocalDateTime;
import java.util.*;

public class Habit implements Aggregate<Id<Habit>> {
  private final Id<Habit> id;
  private List<Tag> tags;
  private String title;
  private String description;
  private Recurrence recurrence;
  private LocalDateTime lastAttendedDate;
  private final Id<Avatar> avatarId;
  private final Optional<String> associatedQuestId;
  private final Optional<String> sourceHabitId;
  private final LocalDateTime createdAt = LocalDateTime.now();

  public Habit(
      Id<Habit> id,
      Id<Avatar> avatarId,
      String title,
      String description,
      Recurrence recurrence,
      Optional<String> associatedQuestId,
      Optional<String> sourceHabitId) {
    this.id = id;
    this.avatarId = avatarId;
    this.title = title;
    this.description = description;
    this.recurrence = recurrence;
    this.associatedQuestId = associatedQuestId;
    this.sourceHabitId = sourceHabitId;
    this.tags = new ArrayList<>();
  }

  public Habit(
      Id<Habit> id,
      Id<Avatar> avatarId,
      String title,
      String description,
      Recurrence recurrence,
      Optional<String> associatedQuestId) {
    this(id, avatarId, title, description, recurrence, associatedQuestId, Optional.empty());
  }

  public void attendHabit(LocalDateTime date) {
    this.lastAttendedDate = date;
  }

  public LocalDateTime nextRecurrence() {
    return this.recurrence.nextAfter(
        Objects.requireNonNullElse(this.lastAttendedDate, this.createdAt));
  }

  public LocalDateTime nextRecurrence(LocalDateTime date) {
    return this.recurrence.nextAfter(date);
  }

  public List<Tag> getTags() {
    return Collections.unmodifiableList(tags);
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public Id<Avatar> getAvatarId() {
    return avatarId;
  }

  public Optional<String> getAssociatedQuestId() {
    return associatedQuestId;
  }

  public Optional<String> getSourceHabitId() {
    return sourceHabitId;
  }

  public Recurrence getRecurrence() {
    return this.recurrence;
  }

  public LocalDateTime getLastAttendedDate() {
    return lastAttendedDate;
  }

  @Override
  public Id<Habit> getId() {
    return this.id;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setTags(List<Tag> tags) {
    this.tags = new ArrayList<>(tags);
  }

  public void setRecurrence(Recurrence recurrence) {
    this.recurrence = recurrence;
  }
}
