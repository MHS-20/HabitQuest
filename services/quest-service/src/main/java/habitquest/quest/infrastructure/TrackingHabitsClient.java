package habitquest.quest.infrastructure;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.quest.application.QuestLogger;
import habitquest.quest.application.TrackingHabitsClientPort;
import habitquest.quest.domain.Avatar;
import habitquest.quest.domain.Habit;
import habitquest.quest.domain.Quest;
import habitquest.quest.domain.reminder.MonthlyRecurrence;
import habitquest.quest.domain.reminder.Recurrence;
import habitquest.quest.domain.reminder.WeeklyRecurrence;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@Component
public class TrackingHabitsClient implements TrackingHabitsClientPort {

  private final RestClient restClient;
  private final QuestLogger log;

  public TrackingHabitsClient(RestClient trackingHabitsRestClient, QuestLogger log) {
    this.restClient = trackingHabitsRestClient;
    this.log = log;
  }

  @Override
  public void createQuestHabitsForAvatar(
      Id<Avatar> avatarId, Id<Quest> questId, List<Habit> habits) {
    for (Habit habit : habits) {
      CreateTrackingHabitRequest request = toRequest(avatarId, questId, habit);
      try {
        restClient.post().uri("/api/v1/habits").body(request).retrieve().toBodilessEntity();
      } catch (RestClientException ex) {
        log.error(request, "Failed to create tracking habit for joined quest", ex);
        throw new TrackingHabitCommunicationException(
            "Failed to create tracking habits for joined quest " + questId.value(), ex);
      }
    }
  }

  private static CreateTrackingHabitRequest toRequest(
      Id<Avatar> avatarId, Id<Quest> questId, Habit habit) {
    Recurrence recurrence = habit.getRecurrence();
    String recurrenceType = "DAILY";
    String dayOfWeek = null;
    Integer dayOfMonth = null;

    if (recurrence instanceof WeeklyRecurrence weekly) {
      recurrenceType = "WEEKLY";
      dayOfWeek = weekly.dayOfWeek().name();
    } else if (recurrence instanceof MonthlyRecurrence monthly) {
      recurrenceType = "MONTHLY";
      dayOfMonth = monthly.dayOfMonth();
    } else {
      // Fallback to DAILY when recurrence is missing or unsupported in quest payload.
      recurrenceType = "DAILY";
    }

    return new CreateTrackingHabitRequest(
        avatarId.value(),
        habit.getTitle(),
        habit.getDescription(),
        recurrenceType,
        dayOfWeek,
        dayOfMonth,
        questId.value(),
        habit.getId().value());
  }

  private record CreateTrackingHabitRequest(
      String avatarId,
      String title,
      String description,
      String recurrenceType,
      String dayOfWeek,
      Integer dayOfMonth,
      String associatedQuestId,
      String sourceHabitId) {}
}
