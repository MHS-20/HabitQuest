package habitquest.avatar_service.domain.events;

import habitquest.avatar_service.domain.stats.AvatarStats;

public record SkillPointAssigned(AvatarStats stats) implements AvatarEvent {}
