package habitquest.avatar.domain.events;

import habitquest.avatar.domain.avatar.Experience;

public record ExperienceIncreased(Experience newExperience) implements AvatarEvent {}
