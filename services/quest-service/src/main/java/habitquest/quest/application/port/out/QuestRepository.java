package habitquest.quest.application.port.out;

import common.ddd.Id;
import common.ddd.Repository;
import common.hexagonal.OutBoundPort;
import habitquest.quest.application.exceptions.QuestNotFoundException;
import habitquest.quest.domain.Quest;
import java.util.List;

@OutBoundPort
public interface QuestRepository extends Repository {
  Quest save(Quest quest);

  List<Quest> findAll();

  Quest findById(Id<Quest> id) throws QuestNotFoundException;

  void deleteById(Id<Quest> id) throws QuestNotFoundException;
}
