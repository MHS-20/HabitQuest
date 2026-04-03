package habitquest.quest.application;

import common.ddd.Id;
import common.hexagonal.OutBoundPort;
import habitquest.quest.domain.Avatar;
import habitquest.quest.domain.Habit;
import habitquest.quest.domain.Quest;
import java.util.List;

@OutBoundPort
public interface TrackingHabitsClientPort {
  void createQuestHabitsForAvatar(Id<Avatar> avatarId, Id<Quest> questId, List<Habit> habits);
}
