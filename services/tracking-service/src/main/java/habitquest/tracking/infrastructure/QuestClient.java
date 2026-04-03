package habitquest.tracking.infrastructure;

import common.ddd.Id;
import common.hexagonal.Adapter;
import habitquest.tracking.application.HabitLogger;
import habitquest.tracking.application.QuestClientPort;
import habitquest.tracking.domain.Avatar;
import habitquest.tracking.domain.Habit;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Adapter
@Component
public class QuestClient implements QuestClientPort {

  public static final String QUEST_CLIENT = "questClient";

  private final RestClient restClient;
  private final HabitLogger log;

  public QuestClient(RestClient questRestClient, HabitLogger log) {
    this.restClient = questRestClient;
    this.log = log;
  }

  @Override
  @CircuitBreaker(name = QUEST_CLIENT, fallbackMethod = "recordHabitAttendanceFallback")
  @Retry(name = QUEST_CLIENT)
  public void recordHabitAttendance(
      String questId, Id<Avatar> avatarId, Id<Habit> habitId, LocalDate attendedOn) {
    AttendanceRequest request =
        new AttendanceRequest(avatarId.value(), habitId.value(), attendedOn.toString());

    log.info(request, "Sending habit attendance to quest-service");
    try {
      restClient
          .post()
          .uri("/api/v1/quests/{id}/attendance", questId)
          .body(request)
          .retrieve()
          .toBodilessEntity();
      log.info(request, "Quest progress updated");
    } catch (RestClientException e) {
      log.error(request, "Failed to update quest progress", e);
      throw new QuestCommunicationException(
          "Failed to update quest progress for quest " + questId, e);
    }
  }

  private void recordHabitAttendanceFallback(
      String questId, Id<Avatar> avatarId, Id<Habit> habitId, LocalDate attendedOn, Exception ex) {
    AttendanceRequest request =
        new AttendanceRequest(avatarId.value(), habitId.value(), attendedOn.toString());
    log.error(request, "Circuit breaker OPEN while updating quest progress", ex);
    throw new QuestCommunicationException(
        "Quest service unavailable for quest " + questId + " during habit attendance", ex);
  }

  private record AttendanceRequest(String avatarId, String habitId, String attendedOn) {}
}
