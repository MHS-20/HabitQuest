package habitquest.avatar.domain.events;

public interface AvatarObserver {
  void notifyAvatarEvent(AvatarEvent event);
}
