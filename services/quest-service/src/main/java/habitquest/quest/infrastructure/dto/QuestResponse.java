package habitquest.quest.infrastructure.dto;

import java.util.List;

public record QuestResponse(
    String id, String name, String duration, Integer reward, List<String> habitIds) {}
