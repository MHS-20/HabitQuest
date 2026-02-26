package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.Experience;

public record ExperienceResetted(Experience experience) implements AvatarEvent {}
