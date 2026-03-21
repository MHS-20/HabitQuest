package habitquest.avatar.domain.events;

import habitquest.avatar.domain.stats.AvatarStat;

public record SkillPointAssigned(String avatarId, AvatarStat stat) implements AvatarEvent {}
