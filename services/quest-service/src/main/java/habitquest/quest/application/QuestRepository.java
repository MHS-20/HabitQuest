package habitquest.quest.application;

import common.ddd.Id;
import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.quest.domain.Quest;

@OutBoundPort
public interface QuestRepository extends Repository {
  Quest save(Quest quest);

  Quest findById(Id<Quest> id) throws QuestNotFoundException;

  void deleteById(Id<Quest> id) throws QuestNotFoundException;
}
