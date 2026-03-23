package habitquest.tracking.infrastructure.dto;

import java.time.LocalDateTime;

public record HabitHistoryEventResponse(
    String eventType, String habitId, String avatarId, LocalDateTime occurredAt, String details) {}
