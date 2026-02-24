package habitquest.avatar_service.domain.events;

import habitquest.avatar_service.domain.avatar.Experience;

public record ExperienceIncreased(Experience newExperience) implements AvatarEvent {
}
