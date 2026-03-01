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
      case LevelUpped l -> handleLevelUpped(l);
      case Dead d -> handleDead(d);
      case SkillPointAssigned s -> handleSkillPointAssigned(s);
      default -> throw new IllegalArgumentException("Unknown event type: " + event.getClass());
    }
  }

  private void handleLevelUpped(LevelUpped event) {
    avatarNotifier.notifyLevelUpped(event);
  }

  private void handleDead(Dead event) {
    avatarNotifier.notifyDead(event);
  }

  private void handleSkillPointAssigned(SkillPointAssigned event) {
    avatarNotifier.notifySkillPointAssigned(event);
  }
}
