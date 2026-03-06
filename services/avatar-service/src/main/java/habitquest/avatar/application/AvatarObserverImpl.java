package habitquest.avatar.application;

import habitquest.avatar.domain.events.*;

public class AvatarObserverImpl implements AvatarObserver {

  private final AvatarNotifier avatarNotifier;

  public AvatarObserverImpl(AvatarNotifier avatarNotifier) {
    this.avatarNotifier = avatarNotifier;
  }

  @Override
  public void notifyAvaterEvent(AvatarEvent event) {
    switch (event) {
      case LevelUpped e -> avatarNotifier.notifyLevelUpped(e);
      case Dead e -> avatarNotifier.notifyDead(e);
      case SkillPointAssigned e -> avatarNotifier.notifySkillPointAssigned(e);
      default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }
  }
}
