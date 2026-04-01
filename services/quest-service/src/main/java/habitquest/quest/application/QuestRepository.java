package habitquest.quest.application;

import common.ddd.Id;
import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.quest.domain.Quest;
import java.util.List;

@OutBoundPort
public interface QuestRepository extends Repository {
  Quest save(Quest quest);

  List<Quest> findAll();

  Quest findById(Id<Quest> id) throws QuestNotFoundException;

  void deleteById(Id<Quest> id) throws QuestNotFoundException;
}
