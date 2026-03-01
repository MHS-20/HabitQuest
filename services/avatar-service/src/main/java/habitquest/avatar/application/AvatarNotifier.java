package habitquest.avatar.application;

import common.hexagonal.OutBoundPort;
import habitquest.avatar.domain.events.Dead;
import habitquest.avatar.domain.events.LevelUpped;
import habitquest.avatar.domain.events.SkillPointAssigned;

@OutBoundPort
public interface AvatarNotifier {
  void notifyLevelUpped(LevelUpped event);

  void notifyDead(Dead event);

  void notifySkillPointAssigned(SkillPointAssigned event);
}
