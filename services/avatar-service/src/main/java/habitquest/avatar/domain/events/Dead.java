package habitquest.avatar.domain.events;

public record Dead(String avatarId) implements AvatarEvent {}
