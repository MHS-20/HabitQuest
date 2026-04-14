package habitquest.quest.infrastructure.dto;

import habitquest.quest.domain.Quest;
import habitquest.quest.infrastructure.dto.QuestRequestsDto.*;
import habitquest.quest.infrastructure.dto.QuestResponsesDto.*;

public class QuestMapper {
  public static QuestResponse toResponse(Quest quest) {
    return new QuestResponse(
        quest.getId().value(),
        quest.getName(),
        quest.getDuration() != null ? (int) quest.getDuration().toDays() : null,
        quest.getReward() != null ? quest.getReward().value() : null,
        quest.getHabits().stream().map(h -> h.getId().value()).toList());
  }
}
