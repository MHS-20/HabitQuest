package habitquest.avatar_service.domain.events;

import habitquest.avatar_service.domain.avatar.Experience;

public record ExperienceResetted(Experience experience) implements AvatarEvent {
}
