package habitquest.quest.infrastructure.dto;

import java.util.List;

public record QuestResponse(
    String id, String name, Integer durationDays, Integer reward, List<String> habitIds) {
  public QuestResponse {
    habitIds = List.copyOf(habitIds);
  }
}
