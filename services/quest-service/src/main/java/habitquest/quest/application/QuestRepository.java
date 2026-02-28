package habitquest.quest.application;

import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.quest.domain.Quest;

@OutBoundPort
public interface QuestRepository extends Repository {
  Quest save(Quest quest);

  Quest findById(String id) throws QuestNotFoundException;

  void deleteById(String id) throws QuestNotFoundException;
}
