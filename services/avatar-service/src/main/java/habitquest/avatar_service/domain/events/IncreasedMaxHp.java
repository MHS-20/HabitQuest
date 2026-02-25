package habitquest.avatar_service.domain.events;

import habitquest.avatar_service.domain.avatar.Health;

public record IncreasedMaxHp(Health health)  implements AvatarEvent {
}
