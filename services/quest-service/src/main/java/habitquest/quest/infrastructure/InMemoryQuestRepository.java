package habitquest.quest.infrastructure;

import common.hexagonal.Adapter;
import habitquest.quest.application.QuestNotFoundException;
import habitquest.quest.application.QuestRepository;
import habitquest.quest.domain.Quest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Adapter
@Repository
public class InMemoryQuestRepository implements QuestRepository {

  private final Map<String, Quest> store = new HashMap<>();

  @Override
  public Quest save(Quest quest) {
    store.put(quest.getId(), quest);
    return quest;
  }

  @Override
  public Quest findById(String id) throws QuestNotFoundException {
    Quest quest = store.get(id);
    if (quest == null) {
      throw new QuestNotFoundException(id);
    }
    return quest;
  }

  @Override
  public void deleteById(String id) throws QuestNotFoundException {
    if (!store.containsKey(id)) {
      throw new QuestNotFoundException(id);
    }
    store.remove(id);
  }
}
