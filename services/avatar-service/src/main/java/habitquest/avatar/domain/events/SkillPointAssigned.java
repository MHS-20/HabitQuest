package habitquest.avatar.domain.events;

import habitquest.avatar.domain.stats.AvatarStat;

public record SkillPointAssigned(AvatarStat stat) implements AvatarEvent {}
