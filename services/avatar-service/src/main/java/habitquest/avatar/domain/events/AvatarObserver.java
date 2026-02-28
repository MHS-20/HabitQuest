package habitquest.avatar.domain.events;

public interface AvatarObserver {
    void notifyAvaterEvent(AvatarEvent event);
}
