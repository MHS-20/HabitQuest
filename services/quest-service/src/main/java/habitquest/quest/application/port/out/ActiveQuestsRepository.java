package habitquest.quest.application.port.out;

import common.ddd.Id;
import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.quest.domain.ActiveQuests;
import habitquest.quest.domain.Avatar;
import habitquest.quest.domain.Quest;
import java.util.List;
import java.util.Optional;

@OutBoundPort
public interface ActiveQuestsRepository extends Repository {
  ActiveQuests save(ActiveQuests activeQuests);

  Optional<ActiveQuests> findByQuestIdAndAvatarId(Id<Quest> questId, Id<Avatar> avatarId);

  List<ActiveQuests> findByAvatarId(Id<Avatar> avatarId);
}
