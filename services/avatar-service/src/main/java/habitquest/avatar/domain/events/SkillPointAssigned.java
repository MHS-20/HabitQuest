package habitquest.avatar.domain.events;

import habitquest.avatar.domain.stats.AvatarStats;

public record SkillPointAssigned(AvatarStats stats) implements AvatarEvent {}
